package dev.engine_room.flywheel.backend.engine.indirect;

import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL46;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.engine_room.flywheel.backend.NoiseTextures;
import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.compile.IndirectPrograms;
import dev.engine_room.flywheel.backend.gl.GlTextureUnit;
import net.minecraft.client.Minecraft;

public class OitFramebuffer {

	private final IndirectPrograms programs;
	private final int vao;

	public int fbo = -1;
	public int depthBounds = -1;
	public int coefficients = -1;
	public int accumulate = -1;

	private int lastWidth = -1;
	private int lastHeight = -1;

	public OitFramebuffer(IndirectPrograms programs) {
		this.programs = programs;
		fbo = GL46.glCreateFramebuffers();
		vao = GL46.glCreateVertexArrays();
	}

	public void depthRange() {
		var mainRenderTarget = Minecraft.getInstance()
				.getMainRenderTarget();

		createTextures(mainRenderTarget.width, mainRenderTarget.height);

		// No depth writes, but we'll still use the depth test
		RenderSystem.depthMask(false);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
		RenderSystem.blendEquation(GL46.GL_MAX);

		GL46.glNamedFramebufferTexture(fbo, GL46.GL_DEPTH_ATTACHMENT, mainRenderTarget.getDepthTextureId(), 0);

		GL46.glNamedFramebufferDrawBuffers(fbo, new int[]{GL46.GL_COLOR_ATTACHMENT0});

		var far = Minecraft.getInstance().gameRenderer.getDepthFar();

		GL46.glClearNamedFramebufferfv(fbo, GL46.GL_COLOR, 0, new float[]{-far, -far, 0, 0});

		GlStateManager._glBindFramebuffer(GL46.GL_FRAMEBUFFER, fbo);
	}

	public void renderTransmittance() {
		// No depth writes, but we'll still use the depth test
		RenderSystem.depthMask(false);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
		RenderSystem.blendEquation(GL46.GL_FUNC_ADD);

		Samplers.DEPTH_RANGE.makeActive();
		GlStateManager._bindTexture(depthBounds);

		Samplers.NOISE.makeActive();
		NoiseTextures.BLUE_NOISE.bind();

		NoiseTextures.BLUE_NOISE.setFilter(true, false);
		GL46.glTextureParameteri(NoiseTextures.BLUE_NOISE.getId(), GL32.GL_TEXTURE_WRAP_S, GL32.GL_REPEAT);
		GL46.glTextureParameteri(NoiseTextures.BLUE_NOISE.getId(), GL32.GL_TEXTURE_WRAP_T, GL32.GL_REPEAT);


		GL46.glNamedFramebufferDrawBuffers(fbo, new int[]{GL46.GL_COLOR_ATTACHMENT1, GL46.GL_COLOR_ATTACHMENT2, GL46.GL_COLOR_ATTACHMENT3, GL46.GL_COLOR_ATTACHMENT4});

		GL46.glClearNamedFramebufferfv(fbo, GL46.GL_COLOR, 0, new float[]{0, 0, 0, 0});
		GL46.glClearNamedFramebufferfv(fbo, GL46.GL_COLOR, 1, new float[]{0, 0, 0, 0});
		GL46.glClearNamedFramebufferfv(fbo, GL46.GL_COLOR, 2, new float[]{0, 0, 0, 0});
		GL46.glClearNamedFramebufferfv(fbo, GL46.GL_COLOR, 3, new float[]{0, 0, 0, 0});

		GlStateManager._glBindFramebuffer(GL46.GL_FRAMEBUFFER, fbo);
	}

	public void shade() {
		// No depth writes, but we'll still use the depth test
		RenderSystem.depthMask(false);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
		RenderSystem.blendEquation(GL46.GL_FUNC_ADD);

		Samplers.DEPTH_RANGE.makeActive();
		GlStateManager._bindTexture(depthBounds);

		Samplers.COEFFICIENTS.makeActive();
		GlStateManager._bindTexture(0);
		GL46.glBindTextureUnit(Samplers.COEFFICIENTS.number, coefficients);

		Samplers.NOISE.makeActive();
		NoiseTextures.BLUE_NOISE.bind();

		GL46.glNamedFramebufferDrawBuffers(fbo, new int[]{GL46.GL_COLOR_ATTACHMENT5});

		GL46.glClearNamedFramebufferfv(fbo, GL46.GL_COLOR, 0, new float[]{0, 0, 0, 0});

		GlStateManager._glBindFramebuffer(GL46.GL_FRAMEBUFFER, fbo);
	}

	public void renderDepth() {
		// No depth writes, but we'll still use the depth test
		RenderSystem.depthMask(true);
		RenderSystem.colorMask(false, false, false, false);
		RenderSystem.disableBlend();

		Samplers.COEFFICIENTS.makeActive();
		GlStateManager._bindTexture(0);
		GL46.glBindTextureUnit(0, coefficients);

		Samplers.DEPTH_RANGE.makeActive();
		GlStateManager._bindTexture(depthBounds);

		GL46.glNamedFramebufferDrawBuffers(fbo, new int[]{});

		programs.getOitDepthProgram()
				.bind();

		// Empty VAO, the actual full screen triangle is generated in the vertex shader
		GlStateManager._glBindVertexArray(vao);

		GL46.glDrawArrays(GL46.GL_TRIANGLES, 0, 3);
	}

	public void composite() {
		// No depth writes, but we'll still use the depth test
		RenderSystem.depthMask(false);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.DestFactor.SRC_ALPHA);
		RenderSystem.blendEquation(GL46.GL_FUNC_ADD);

		var mainRenderTarget = Minecraft.getInstance()
				.getMainRenderTarget();

		mainRenderTarget.bindWrite(false);

		GlTextureUnit.T0.makeActive();
		GlStateManager._bindTexture(0);
		GL46.glBindTextureUnit(0, coefficients);

		GlTextureUnit.T1.makeActive();
		GlStateManager._bindTexture(accumulate);

		programs.getOitCompositeProgram()
				.bind();

		// Empty VAO, the actual full screen triangle is generated in the vertex shader
		GlStateManager._glBindVertexArray(vao);

		GL46.glDrawArrays(GL46.GL_TRIANGLES, 0, 3);
	}

	public void delete() {
		deleteTextures();
		GL46.glDeleteFramebuffers(fbo);
		GL46.glDeleteVertexArrays(vao);
	}

	private void deleteTextures() {
		if (depthBounds != -1) {
			GL46.glDeleteTextures(depthBounds);
		}
		if (coefficients != -1) {
			GL46.glDeleteTextures(coefficients);
		}
		if (accumulate != -1) {
			GL46.glDeleteTextures(accumulate);
		}
	}

	private void createTextures(int width, int height) {
		if (lastWidth == width && lastHeight == height) {
			return;
		}

		lastWidth = width;
		lastHeight = height;

		deleteTextures();

		depthBounds = GL46.glCreateTextures(GL46.GL_TEXTURE_2D);
		coefficients = GL46.glCreateTextures(GL46.GL_TEXTURE_2D_ARRAY);
		accumulate = GL46.glCreateTextures(GL46.GL_TEXTURE_2D);

		GL46.glTextureStorage2D(depthBounds, 1, GL32.GL_RG32F, width, height);
		GL46.glTextureStorage3D(coefficients, 1, GL32.GL_RGBA16F, width, height, 4);

		GL46.glTextureStorage2D(accumulate, 1, GL32.GL_RGBA16F, width, height);

		//		for (int tex : new int[]{zerothMoment, moments, composite}) {
		//			GL46.glTextureParameteri(tex, GL32.GL_TEXTURE_MIN_FILTER, GL32.GL_NEAREST);
		//			GL46.glTextureParameteri(tex, GL32.GL_TEXTURE_MAG_FILTER, GL32.GL_NEAREST);
		//			GL46.glTextureParameteri(tex, GL32.GL_TEXTURE_COMPARE_MODE, GL32.GL_NONE);
		//			GL46.glTextureParameteri(tex, GL32.GL_TEXTURE_WRAP_S, GL32.GL_CLAMP_TO_EDGE);
		//			GL46.glTextureParameteri(tex, GL32.GL_TEXTURE_WRAP_T, GL32.GL_CLAMP_TO_EDGE);
		//		}

		GL46.glNamedFramebufferTexture(fbo, GL46.GL_COLOR_ATTACHMENT0, depthBounds, 0);
		GL46.glNamedFramebufferTextureLayer(fbo, GL46.GL_COLOR_ATTACHMENT1, coefficients, 0, 0);
		GL46.glNamedFramebufferTextureLayer(fbo, GL46.GL_COLOR_ATTACHMENT2, coefficients, 0, 1);
		GL46.glNamedFramebufferTextureLayer(fbo, GL46.GL_COLOR_ATTACHMENT3, coefficients, 0, 2);
		GL46.glNamedFramebufferTextureLayer(fbo, GL46.GL_COLOR_ATTACHMENT4, coefficients, 0, 3);
		GL46.glNamedFramebufferTexture(fbo, GL46.GL_COLOR_ATTACHMENT5, accumulate, 0);
	}
}
