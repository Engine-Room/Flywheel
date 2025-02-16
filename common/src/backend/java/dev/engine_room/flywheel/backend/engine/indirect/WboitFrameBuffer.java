package dev.engine_room.flywheel.backend.engine.indirect;

import org.lwjgl.opengl.ARBDrawBuffersBlend;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL46;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.engine_room.flywheel.backend.compile.IndirectPrograms;
import dev.engine_room.flywheel.backend.gl.GlTextureUnit;
import net.minecraft.client.Minecraft;

public class WboitFrameBuffer {

	public final int fbo;
	private final IndirectPrograms programs;
	private final int vao;

	public int accum;
	public int reveal;

	private int lastWidth = -1;
	private int lastHeight = -1;

	public WboitFrameBuffer(IndirectPrograms programs) {
		this.programs = programs;
		fbo = GL46.glCreateFramebuffers();
		vao = GL46.glCreateVertexArrays();
	}

	public void setup() {
		var mainRenderTarget = Minecraft.getInstance()
				.getMainRenderTarget();

		createTextures(mainRenderTarget.width, mainRenderTarget.height);

		// No depth writes, but we'll still use the depth test
		GlStateManager._depthMask(false);
		GlStateManager._enableBlend();
		ARBDrawBuffersBlend.glBlendFunciARB(0, GL46.GL_ONE, GL46.GL_ONE); // accumulation blend target
		ARBDrawBuffersBlend.glBlendFunciARB(1, GL46.GL_ZERO, GL46.GL_ONE_MINUS_SRC_COLOR); // revealage blend target
		GlStateManager._blendEquation(GL46.GL_FUNC_ADD);

		GL46.glNamedFramebufferTexture(fbo, GL46.GL_DEPTH_ATTACHMENT, mainRenderTarget.getDepthTextureId(), 0);

		GlStateManager._glBindFramebuffer(GL46.GL_FRAMEBUFFER, fbo);

		GL46.glClearBufferfv(GL46.GL_COLOR, 0, new float[]{0, 0, 0, 0});
		GL46.glClearBufferfv(GL46.GL_COLOR, 1, new float[]{1, 1, 1, 1});
	}

	public void composite() {
		var mainRenderTarget = Minecraft.getInstance()
				.getMainRenderTarget();

		mainRenderTarget.bindWrite(false);

		var oitCompositeProgram = programs.getOitCompositeProgram();

		GlStateManager._depthMask(false);
		GlStateManager._depthFunc(GL46.GL_ALWAYS);
		GlStateManager._enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		oitCompositeProgram.bind();

		GlTextureUnit.T0.makeActive();
		GlStateManager._bindTexture(accum);

		GlTextureUnit.T1.makeActive();
		GlStateManager._bindTexture(reveal);

		// Empty VAO, the actual full screen triangle is generated in the vertex shader
		GlStateManager._glBindVertexArray(vao);

		GL46.glDrawArrays(GL46.GL_TRIANGLES, 0, 3);
	}

	public void delete() {
		GL46.glDeleteTextures(accum);
		GL46.glDeleteTextures(reveal);
		GL46.glDeleteFramebuffers(fbo);
		GL46.glDeleteVertexArrays(vao);
	}

	private void createTextures(int width, int height) {
		if (lastWidth == width && lastHeight == height) {
			return;
		}

		lastWidth = width;
		lastHeight = height;

		GL46.glDeleteTextures(accum);
		GL46.glDeleteTextures(reveal);

		accum = GL46.glCreateTextures(GL46.GL_TEXTURE_2D);
		reveal = GL46.glCreateTextures(GL46.GL_TEXTURE_2D);

		GL46.glNamedFramebufferDrawBuffers(fbo, new int[]{GL46.GL_COLOR_ATTACHMENT0, GL46.GL_COLOR_ATTACHMENT1});

		GL46.glNamedFramebufferTexture(fbo, GL46.GL_COLOR_ATTACHMENT0, accum, 0);
		GL46.glNamedFramebufferTexture(fbo, GL46.GL_COLOR_ATTACHMENT1, reveal, 0);

		GL46.glTextureStorage2D(accum, 1, GL32.GL_RGBA32F, width, height);
		GL46.glTextureStorage2D(reveal, 1, GL32.GL_R8, width, height);

		for (int tex : new int[]{accum, reveal}) {
			GL46.glTextureParameteri(tex, GL32.GL_TEXTURE_MIN_FILTER, GL32.GL_NEAREST);
			GL46.glTextureParameteri(tex, GL32.GL_TEXTURE_MAG_FILTER, GL32.GL_NEAREST);
			GL46.glTextureParameteri(tex, GL32.GL_TEXTURE_COMPARE_MODE, GL32.GL_NONE);
			GL46.glTextureParameteri(tex, GL32.GL_TEXTURE_WRAP_S, GL32.GL_CLAMP_TO_EDGE);
			GL46.glTextureParameteri(tex, GL32.GL_TEXTURE_WRAP_T, GL32.GL_CLAMP_TO_EDGE);
		}
	}
}
