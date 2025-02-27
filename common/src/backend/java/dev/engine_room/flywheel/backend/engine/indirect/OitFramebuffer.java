package dev.engine_room.flywheel.backend.engine.indirect;

import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL46;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.engine_room.flywheel.backend.NoiseTextures;
import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.compile.OitPrograms;
import dev.engine_room.flywheel.backend.gl.GlCompat;
import dev.engine_room.flywheel.backend.gl.GlTextureUnit;
import net.minecraft.client.Minecraft;

public class OitFramebuffer {
	public static final float[] CLEAR_TO_ZERO = {0, 0, 0, 0};
	public static final int[] DEPTH_RANGE_DRAW_BUFFERS = {GL46.GL_COLOR_ATTACHMENT0};
	public static final int[] RENDER_TRANSMITTANCE_DRAW_BUFFERS = {GL46.GL_COLOR_ATTACHMENT1, GL46.GL_COLOR_ATTACHMENT2, GL46.GL_COLOR_ATTACHMENT3, GL46.GL_COLOR_ATTACHMENT4};
	public static final int[] ACCUMULATE_DRAW_BUFFERS = {GL46.GL_COLOR_ATTACHMENT5};
	public static final int[] DEPTH_ONLY_DRAW_BUFFERS = {};

	private final OitPrograms programs;
	private final int vao;

	public int fbo = -1;
	public int depthBounds = -1;
	public int coefficients = -1;
	public int accumulate = -1;

	private int lastWidth = -1;
	private int lastHeight = -1;

	public OitFramebuffer(OitPrograms programs) {
		this.programs = programs;
		if (GlCompat.SUPPORTS_DSA) {
			vao = GL46.glCreateVertexArrays();
		} else {
			vao = GL32.glGenVertexArrays();
		}
	}

	/**
	 * Set up the framebuffer.
	 */
	public void prepare() {
		RenderTarget renderTarget;

		if (Minecraft.useShaderTransparency()) {
			renderTarget = Minecraft.getInstance().levelRenderer.getItemEntityTarget();

			renderTarget.copyDepthFrom(Minecraft.getInstance()
					.getMainRenderTarget());
		} else {
			renderTarget = Minecraft.getInstance()
					.getMainRenderTarget();
		}

		maybeResizeFBO(renderTarget.width, renderTarget.height);

		Samplers.COEFFICIENTS.makeActive();
		// Bind zero to render system to make sure we clear their internal state
		RenderSystem.bindTexture(0);
		GL32.glBindTexture(GL32.GL_TEXTURE_2D_ARRAY, coefficients);

		Samplers.DEPTH_RANGE.makeActive();
		RenderSystem.bindTexture(depthBounds);

		Samplers.NOISE.makeActive();
		NoiseTextures.BLUE_NOISE.bind();

		GlStateManager._glBindFramebuffer(GL32.GL_FRAMEBUFFER, fbo);
		GL32.glFramebufferTexture(GL32.GL_FRAMEBUFFER, GL32.GL_DEPTH_ATTACHMENT, renderTarget.getDepthTextureId(), 0);
	}

	/**
	 * Render out the min and max depth per fragment.
	 */
	public void depthRange() {
		// No depth writes, but we'll still use the depth test.
		RenderSystem.depthMask(false);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
		RenderSystem.blendEquation(GL32.GL_MAX);

		var far = Minecraft.getInstance().gameRenderer.getDepthFar();

		if (GlCompat.SUPPORTS_DSA) {
			GL46.glNamedFramebufferDrawBuffers(fbo, DEPTH_RANGE_DRAW_BUFFERS);
			GL46.glClearNamedFramebufferfv(fbo, GL46.GL_COLOR, 0, new float[]{-far, -far, 0, 0});
		} else {
			GL32.glDrawBuffers(DEPTH_RANGE_DRAW_BUFFERS);
			RenderSystem.clearColor(-far, -far, 0, 0);
			RenderSystem.clear(GL32.GL_COLOR_BUFFER_BIT, false);
		}
	}

	/**
	 * Generate the coefficients to the transmittance function.
	 */
	public void renderTransmittance() {
		// No depth writes, but we'll still use the depth test
		RenderSystem.depthMask(false);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
		RenderSystem.blendEquation(GL32.GL_FUNC_ADD);

		if (GlCompat.SUPPORTS_DSA) {
			GL46.glNamedFramebufferDrawBuffers(fbo, RENDER_TRANSMITTANCE_DRAW_BUFFERS);

			GL46.glClearNamedFramebufferfv(fbo, GL46.GL_COLOR, 0, CLEAR_TO_ZERO);
			GL46.glClearNamedFramebufferfv(fbo, GL46.GL_COLOR, 1, CLEAR_TO_ZERO);
			GL46.glClearNamedFramebufferfv(fbo, GL46.GL_COLOR, 2, CLEAR_TO_ZERO);
			GL46.glClearNamedFramebufferfv(fbo, GL46.GL_COLOR, 3, CLEAR_TO_ZERO);
		} else {
			GL32.glDrawBuffers(RENDER_TRANSMITTANCE_DRAW_BUFFERS);
			RenderSystem.clearColor(0, 0, 0, 0);
			RenderSystem.clear(GL32.GL_COLOR_BUFFER_BIT, false);
		}
	}

	/**
	 * If any fragment has its transmittance fall off to zero, search the transmittance
	 * function to determine at what depth that occurs and write out to the depth buffer.
	 */
	public void renderDepthFromTransmittance() {
		// Only write to depth, not color.
		RenderSystem.depthMask(true);
		RenderSystem.colorMask(false, false, false, false);
		RenderSystem.disableBlend();
		RenderSystem.depthFunc(GL32.GL_ALWAYS);

		if (GlCompat.SUPPORTS_DSA) {
			GL46.glNamedFramebufferDrawBuffers(fbo, DEPTH_ONLY_DRAW_BUFFERS);
		} else {
			GL32.glDrawBuffers(DEPTH_ONLY_DRAW_BUFFERS);
		}

		programs.getOitDepthProgram()
				.bind();

		drawFullscreenQuad();
	}

	/**
	 * Sample the transmittance function and accumulate.
	 */
	public void accumulate() {
		// No depth writes, but we'll still use the depth test
		RenderSystem.depthMask(false);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
		RenderSystem.blendEquation(GL32.GL_FUNC_ADD);

		if (GlCompat.SUPPORTS_DSA) {
			GL46.glNamedFramebufferDrawBuffers(fbo, ACCUMULATE_DRAW_BUFFERS);

			GL46.glClearNamedFramebufferfv(fbo, GL46.GL_COLOR, 0, CLEAR_TO_ZERO);
		} else {
			GL32.glDrawBuffers(ACCUMULATE_DRAW_BUFFERS);
			RenderSystem.clearColor(0, 0, 0, 0);
			RenderSystem.clear(GL32.GL_COLOR_BUFFER_BIT, false);
		}
	}

	/**
	 * Composite the accumulated luminance onto the main framebuffer.
	 */
	public void composite() {
		if (Minecraft.useShaderTransparency()) {
			Minecraft.getInstance().levelRenderer.getItemEntityTarget()
					.bindWrite(false);
		} else {
			Minecraft.getInstance()
					.getMainRenderTarget()
					.bindWrite(false);
		}

		// The composite shader writes out the closest depth to gl_FragDepth.
		// depthMask = true: OIT stuff renders on top of other transparent stuff.
		// depthMask = false: other transparent stuff renders on top of OIT stuff.
		// If Neo gets wavelet OIT we can use their hooks to be correct with everything.
		RenderSystem.depthMask(true);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.enableBlend();

		// We rely on the blend func to achieve:
		// final color = (1 - transmittance_total) * sum(color_f * alpha_f * transmittance_f) / sum(alpha_f * transmittance_f)
		//			+ color_dst * transmittance_total
		//
		// Though note that the alpha value we emit in the fragment shader is actually (1. - transmittance_total).
		// The extra inversion step is so we can have a sane alpha value written out for the fabulous blit shader to consume.
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.blendEquation(GL32.GL_FUNC_ADD);
		RenderSystem.depthFunc(GL32.GL_ALWAYS);

		GlTextureUnit.T0.makeActive();
		RenderSystem.bindTexture(accumulate);

		programs.getOitCompositeProgram()
				.bind();

		drawFullscreenQuad();

		Minecraft.getInstance()
				.getMainRenderTarget()
				.bindWrite(false);
	}

	public void delete() {
		deleteTextures();
		GL32.glDeleteVertexArrays(vao);
	}

	private void drawFullscreenQuad() {
		// Empty VAO, the actual full screen triangle is generated in the vertex shader
		GlStateManager._glBindVertexArray(vao);

		GL32.glDrawArrays(GL32.GL_TRIANGLES, 0, 3);
	}

	private void deleteTextures() {
		if (depthBounds != -1) {
			GL32.glDeleteTextures(depthBounds);
		}
		if (coefficients != -1) {
			GL32.glDeleteTextures(coefficients);
		}
		if (accumulate != -1) {
			GL32.glDeleteTextures(accumulate);
		}
		if (fbo != -1) {
			GL32.glDeleteFramebuffers(fbo);
		}

		// We sometimes get the same texture ID back when creating new textures,
		// so bind zero to clear the GlStateManager
		Samplers.COEFFICIENTS.makeActive();
		RenderSystem.bindTexture(0);
		Samplers.DEPTH_RANGE.makeActive();
		RenderSystem.bindTexture(0);
	}

	private void maybeResizeFBO(int width, int height) {
		if (lastWidth == width && lastHeight == height) {
			return;
		}

		lastWidth = width;
		lastHeight = height;

		deleteTextures();

		if (GlCompat.SUPPORTS_DSA) {
			fbo = GL46.glCreateFramebuffers();

			depthBounds = GL46.glCreateTextures(GL46.GL_TEXTURE_2D);
			coefficients = GL46.glCreateTextures(GL46.GL_TEXTURE_2D_ARRAY);
			accumulate = GL46.glCreateTextures(GL46.GL_TEXTURE_2D);

			GL46.glTextureStorage2D(depthBounds, 1, GL32.GL_RG32F, width, height);
			GL46.glTextureStorage3D(coefficients, 1, GL32.GL_RGBA16F, width, height, 4);
			GL46.glTextureStorage2D(accumulate, 1, GL32.GL_RGBA16F, width, height);

			GL46.glNamedFramebufferTexture(fbo, GL32.GL_COLOR_ATTACHMENT0, depthBounds, 0);
			GL46.glNamedFramebufferTextureLayer(fbo, GL32.GL_COLOR_ATTACHMENT1, coefficients, 0, 0);
			GL46.glNamedFramebufferTextureLayer(fbo, GL32.GL_COLOR_ATTACHMENT2, coefficients, 0, 1);
			GL46.glNamedFramebufferTextureLayer(fbo, GL32.GL_COLOR_ATTACHMENT3, coefficients, 0, 2);
			GL46.glNamedFramebufferTextureLayer(fbo, GL32.GL_COLOR_ATTACHMENT4, coefficients, 0, 3);
			GL46.glNamedFramebufferTexture(fbo, GL32.GL_COLOR_ATTACHMENT5, accumulate, 0);
		} else {
			fbo = GL46.glGenFramebuffers();

			depthBounds = GL32.glGenTextures();
			coefficients = GL32.glGenTextures();
			accumulate = GL32.glGenTextures();

			GlTextureUnit.T0.makeActive();
			RenderSystem.bindTexture(0);

			GL32.glBindTexture(GL32.GL_TEXTURE_2D, depthBounds);
			GL32.glTexImage2D(GL32.GL_TEXTURE_2D, 0, GL32.GL_RG32F, width, height, 0, GL46.GL_RGBA, GL46.GL_BYTE, 0);

			GL32.glTexParameteri(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_MIN_FILTER, GL32.GL_NEAREST);
			GL32.glTexParameteri(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_MAG_FILTER, GL32.GL_NEAREST);
			GL32.glTexParameteri(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_WRAP_S, GL32.GL_CLAMP_TO_EDGE);
			GL32.glTexParameteri(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_WRAP_T, GL32.GL_CLAMP_TO_EDGE);

			GL32.glBindTexture(GL32.GL_TEXTURE_2D_ARRAY, coefficients);
			GL32.glTexImage3D(GL32.GL_TEXTURE_2D_ARRAY, 0, GL32.GL_RGBA16F, width, height, 4, 0, GL46.GL_RGBA, GL46.GL_BYTE, 0);

			GL32.glTexParameteri(GL32.GL_TEXTURE_2D_ARRAY, GL32.GL_TEXTURE_MIN_FILTER, GL32.GL_NEAREST);
			GL32.glTexParameteri(GL32.GL_TEXTURE_2D_ARRAY, GL32.GL_TEXTURE_MAG_FILTER, GL32.GL_NEAREST);
			GL32.glTexParameteri(GL32.GL_TEXTURE_2D_ARRAY, GL32.GL_TEXTURE_WRAP_S, GL32.GL_CLAMP_TO_EDGE);
			GL32.glTexParameteri(GL32.GL_TEXTURE_2D_ARRAY, GL32.GL_TEXTURE_WRAP_T, GL32.GL_CLAMP_TO_EDGE);

			GL32.glBindTexture(GL32.GL_TEXTURE_2D, accumulate);
			GL32.glTexImage2D(GL32.GL_TEXTURE_2D, 0, GL32.GL_RGBA16F, width, height, 0, GL46.GL_RGBA, GL46.GL_BYTE, 0);

			GL32.glTexParameteri(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_MIN_FILTER, GL32.GL_NEAREST);
			GL32.glTexParameteri(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_MAG_FILTER, GL32.GL_NEAREST);
			GL32.glTexParameteri(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_WRAP_S, GL32.GL_CLAMP_TO_EDGE);
			GL32.glTexParameteri(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_WRAP_T, GL32.GL_CLAMP_TO_EDGE);

			GlStateManager._glBindFramebuffer(GL32.GL_FRAMEBUFFER, fbo);

			GL46.glFramebufferTexture(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT0, depthBounds, 0);
			GL46.glFramebufferTextureLayer(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT1, coefficients, 0, 0);
			GL46.glFramebufferTextureLayer(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT2, coefficients, 0, 1);
			GL46.glFramebufferTextureLayer(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT3, coefficients, 0, 2);
			GL46.glFramebufferTextureLayer(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT4, coefficients, 0, 3);
			GL46.glFramebufferTexture(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT5, accumulate, 0);
		}
	}
}
