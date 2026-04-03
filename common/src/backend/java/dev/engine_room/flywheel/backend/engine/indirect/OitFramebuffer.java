package dev.engine_room.flywheel.backend.engine.indirect;

import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL46;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.engine_room.flywheel.backend.NoiseTextures;
import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.compile.OitPrograms;
import dev.engine_room.flywheel.backend.gl.GlCompat;
import dev.engine_room.flywheel.backend.gl.GlTextureUnit;
import dev.engine_room.flywheel.backend.mixin.GpuDeviceAccessor;
import net.minecraft.client.Minecraft;

public class OitFramebuffer {
	public static final float[] CLEAR_TO_ZERO = {0, 0, 0, 0};
	public static final int[] DEPTH_RANGE_DRAW_BUFFERS = {GlConst.GL_COLOR_ATTACHMENT0};
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
			vao = GlStateManager._glGenVertexArrays();
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
		// Bind zero to state manager to make sure we clear its internal state
		GlStateManager._bindTexture(0);
		GL32.glBindTexture(GL32.GL_TEXTURE_2D_ARRAY, coefficients);

		Samplers.DEPTH_RANGE.makeActive();
		GlStateManager._bindTexture(depthBounds);

		Samplers.NOISE.makeActive();
		GlStateManager._bindTexture(((GlTexture) NoiseTextures.BLUE_NOISE.getTexture()).glId());

		GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, fbo);
		GlTexture depthTexture = (GlTexture) renderTarget.getDepthTexture();
		GL32.glFramebufferTexture(GlConst.GL_FRAMEBUFFER, GlConst.GL_DEPTH_ATTACHMENT, depthTexture != null ? depthTexture.glId() : 0, 0);
	}

	/**
	 * Render out the min and max depth per fragment.
	 */
	public void depthRange() {
		// No depth writes, but we'll still use the depth test.
		GlStateManager._depthMask(false);
		GlStateManager._colorMask(0b1111);
		GlStateManager._enableBlend();
		GlStateManager._blendFuncSeparate(GlConst.GL_ONE, GlConst.GL_ONE, GlConst.GL_ONE, GlConst.GL_ONE);
		GL32.glBlendEquation(GlConst.GL_MAX);

		var far = Minecraft.getInstance().gameRenderer.getGameRenderState().levelRenderState.cameraRenderState.depthFar;

		if (GlCompat.SUPPORTS_DSA) {
			GL46.glNamedFramebufferDrawBuffers(fbo, DEPTH_RANGE_DRAW_BUFFERS);
			GL46.glClearNamedFramebufferfv(fbo, GL46.GL_COLOR, 0, new float[]{-far, -far, 0, 0});
		} else {
			GL32.glDrawBuffers(DEPTH_RANGE_DRAW_BUFFERS);
			GL32.glClearColor(-far, -far, 0, 0);
			GlStateManager._clear(GlConst.GL_COLOR_BUFFER_BIT);
		}
	}

	/**
	 * Generate the coefficients to the transmittance function.
	 */
	public void renderTransmittance() {
		// No depth writes, but we'll still use the depth test
		GlStateManager._depthMask(false);
		GlStateManager._colorMask(0b1111);
		GlStateManager._enableBlend();
		GlStateManager._blendFuncSeparate(GlConst.GL_ONE, GlConst.GL_ONE, GlConst.GL_ONE, GlConst.GL_ONE);
		GL32.glBlendEquation(GlConst.GL_FUNC_ADD);

		if (GlCompat.SUPPORTS_DSA) {
			GL46.glNamedFramebufferDrawBuffers(fbo, RENDER_TRANSMITTANCE_DRAW_BUFFERS);

			GL46.glClearNamedFramebufferfv(fbo, GL46.GL_COLOR, 0, CLEAR_TO_ZERO);
			GL46.glClearNamedFramebufferfv(fbo, GL46.GL_COLOR, 1, CLEAR_TO_ZERO);
			GL46.glClearNamedFramebufferfv(fbo, GL46.GL_COLOR, 2, CLEAR_TO_ZERO);
			GL46.glClearNamedFramebufferfv(fbo, GL46.GL_COLOR, 3, CLEAR_TO_ZERO);
		} else {
			GL32.glDrawBuffers(RENDER_TRANSMITTANCE_DRAW_BUFFERS);
			GL32.glClearColor(0, 0, 0, 0);
			GlStateManager._clear(GlConst.GL_COLOR_BUFFER_BIT);
		}
	}

	/**
	 * If any fragment has its transmittance fall off to zero, search the transmittance
	 * function to determine at what depth that occurs and write out to the depth buffer.
	 */
	public void renderDepthFromTransmittance() {
		// Only write to depth, not color.
		GlStateManager._depthMask(true);
		GlStateManager._colorMask(0b0000);
		GlStateManager._disableBlend();
		GlStateManager._depthFunc(GlConst.GL_ALWAYS);

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
		GlStateManager._depthMask(false);
		GlStateManager._colorMask(0b1111);
		GlStateManager._enableBlend();
		GlStateManager._blendFuncSeparate(GlConst.GL_ONE, GlConst.GL_ONE, GlConst.GL_ONE, GlConst.GL_ONE);
		GL32.glBlendEquation(GlConst.GL_FUNC_ADD);

		if (GlCompat.SUPPORTS_DSA) {
			GL46.glNamedFramebufferDrawBuffers(fbo, ACCUMULATE_DRAW_BUFFERS);

			GL46.glClearNamedFramebufferfv(fbo, GL46.GL_COLOR, 0, CLEAR_TO_ZERO);
		} else {
			GL32.glDrawBuffers(ACCUMULATE_DRAW_BUFFERS);
			GL32.glClearColor(0, 0, 0, 0);
			GlStateManager._clear(GlConst.GL_COLOR_BUFFER_BIT);
		}
	}

	/**
	 * Composite the accumulated luminance onto the main framebuffer.
	 */
	public void composite() {
		if (Minecraft.useShaderTransparency()) {
			bindRenderTarget(Minecraft.getInstance().levelRenderer.getItemEntityTarget());
		} else {
			bindRenderTarget(Minecraft.getInstance().getMainRenderTarget());
		}

		// The composite shader writes out the closest depth to gl_FragDepth.
		// depthMask = true: OIT stuff renders on top of other transparent stuff.
		// depthMask = false: other transparent stuff renders on top of OIT stuff.
		// If Neo gets wavelet OIT we can use their hooks to be correct with everything.
		GlStateManager._depthMask(true);
		GlStateManager._colorMask(0b1111);
		GlStateManager._enableBlend();

		// We rely on the blend func to achieve:
		// final color = (1 - transmittance_total) * sum(color_f * alpha_f * transmittance_f) / sum(alpha_f * transmittance_f)
		//			+ color_dst * transmittance_total
		//
		// Though note that the alpha value we emit in the fragment shader is actually (1. - transmittance_total).
		// The extra inversion step is so we can have a sane alpha value written out for the fabulous blit shader to consume.
		GlStateManager._blendFuncSeparate(GlConst.GL_SRC_ALPHA, GlConst.GL_ONE_MINUS_SRC_ALPHA, GlConst.GL_ONE, GlConst.GL_ONE_MINUS_SRC_ALPHA);
		GL32.glBlendEquation(GlConst.GL_FUNC_ADD);
		GlStateManager._depthFunc(GlConst.GL_ALWAYS);

		GlTextureUnit.T0.makeActive();
		GlStateManager._bindTexture(accumulate);

		programs.getOitCompositeProgram()
				.bind();

		drawFullscreenQuad();

		bindRenderTarget(Minecraft.getInstance().getMainRenderTarget());
	}

	private static void bindRenderTarget(RenderTarget target) {
		GlTexture colorTexture = (GlTexture) target.getColorTexture();
		int i = colorTexture.getFbo(
				((GlDevice) ((GpuDeviceAccessor) RenderSystem.getDevice()).getBackend()).directStateAccess(),
				target.getDepthTexture()
		);
		GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, i);
		GlStateManager._viewport(0, 0, colorTexture.getWidth(0), colorTexture.getHeight(0));
	}

	public void delete() {
		deleteTextures();
		GL32.glDeleteVertexArrays(vao);
	}

	private void drawFullscreenQuad() {
		// Empty VAO, the actual full screen triangle is generated in the vertex shader
		GlStateManager._glBindVertexArray(vao);

		GlStateManager._drawArrays(GlConst.GL_TRIANGLES, 0, 3);
	}

	private void deleteTextures() {
		if (depthBounds != -1) {
			GlStateManager._deleteTexture(depthBounds);
		}
		if (coefficients != -1) {
			GlStateManager._deleteTexture(coefficients);
		}
		if (accumulate != -1) {
			GlStateManager._deleteTexture(accumulate);
		}
		if (fbo != -1) {
			GlStateManager._glDeleteFramebuffers(fbo);
		}

		// We sometimes get the same texture ID back when creating new textures,
		// so bind zero to clear the GlStateManager
		Samplers.COEFFICIENTS.makeActive();
		GlStateManager._bindTexture(0);
		Samplers.DEPTH_RANGE.makeActive();
		GlStateManager._bindTexture(0);
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

			GL46.glNamedFramebufferTexture(fbo, GlConst.GL_COLOR_ATTACHMENT0, depthBounds, 0);
			GL46.glNamedFramebufferTextureLayer(fbo, GL32.GL_COLOR_ATTACHMENT1, coefficients, 0, 0);
			GL46.glNamedFramebufferTextureLayer(fbo, GL32.GL_COLOR_ATTACHMENT2, coefficients, 0, 1);
			GL46.glNamedFramebufferTextureLayer(fbo, GL32.GL_COLOR_ATTACHMENT3, coefficients, 0, 2);
			GL46.glNamedFramebufferTextureLayer(fbo, GL32.GL_COLOR_ATTACHMENT4, coefficients, 0, 3);
			GL46.glNamedFramebufferTexture(fbo, GL32.GL_COLOR_ATTACHMENT5, accumulate, 0);
		} else {
			fbo = GL46.glGenFramebuffers();

			depthBounds = GlStateManager._genTexture();
			coefficients = GlStateManager._genTexture();
			accumulate = GlStateManager._genTexture();

			GlTextureUnit.T0.makeActive();
			GlStateManager._bindTexture(0);

			GlStateManager._bindTexture(depthBounds);
			GL32.glTexImage2D(GlConst.GL_TEXTURE_2D, 0, GL32.GL_RG32F, width, height, 0, GL46.GL_RGBA, GL46.GL_BYTE, 0);

			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_NEAREST);
			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_NEAREST);
			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_CLAMP_TO_EDGE);
			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_CLAMP_TO_EDGE);

			GL32.glBindTexture(GL32.GL_TEXTURE_2D_ARRAY, coefficients);
			GL32.glTexImage3D(GL32.GL_TEXTURE_2D_ARRAY, 0, GL32.GL_RGBA16F, width, height, 4, 0, GL46.GL_RGBA, GL46.GL_BYTE, 0);

			GlStateManager._texParameter(GL32.GL_TEXTURE_2D_ARRAY, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_NEAREST);
			GlStateManager._texParameter(GL32.GL_TEXTURE_2D_ARRAY, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_NEAREST);
			GlStateManager._texParameter(GL32.GL_TEXTURE_2D_ARRAY, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_CLAMP_TO_EDGE);
			GlStateManager._texParameter(GL32.GL_TEXTURE_2D_ARRAY, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_CLAMP_TO_EDGE);

			GlStateManager._bindTexture(accumulate);
			GL32.glTexImage2D(GlConst.GL_TEXTURE_2D, 0, GL32.GL_RGBA16F, width, height, 0, GL46.GL_RGBA, GL46.GL_BYTE, 0);

			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_NEAREST);
			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_NEAREST);
			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_CLAMP_TO_EDGE);
			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_CLAMP_TO_EDGE);

			GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, fbo);

			GL46.glFramebufferTexture(GlConst.GL_FRAMEBUFFER, GlConst.GL_COLOR_ATTACHMENT0, depthBounds, 0);
			GL46.glFramebufferTextureLayer(GlConst.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT1, coefficients, 0, 0);
			GL46.glFramebufferTextureLayer(GlConst.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT2, coefficients, 0, 1);
			GL46.glFramebufferTextureLayer(GlConst.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT3, coefficients, 0, 2);
			GL46.glFramebufferTextureLayer(GlConst.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT4, coefficients, 0, 3);
			GL46.glFramebufferTexture(GlConst.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT5, accumulate, 0);
		}
	}
}
