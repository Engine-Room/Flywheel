package dev.engine_room.flywheel.backend.engine.indirect;

import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL46;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.compile.IndirectPrograms;
import dev.engine_room.flywheel.backend.gl.GlTextureUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class MboitFramebuffer {

	public final int fbo;
	private final IndirectPrograms programs;
	private final int vao;

	public int zerothMoment;
	public int moments0;
	public int moments1;
	public int accumulate;

	private int lastWidth = -1;
	private int lastHeight = -1;

	public MboitFramebuffer(IndirectPrograms programs) {
		this.programs = programs;
		fbo = GL46.glCreateFramebuffers();
		vao = GL46.glCreateVertexArrays();
	}

	public void generateMoments() {
		var mainRenderTarget = Minecraft.getInstance()
				.getMainRenderTarget();

		createTextures(mainRenderTarget.width, mainRenderTarget.height);

		// No depth writes, but we'll still use the depth test
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
		RenderSystem.blendEquation(GL46.GL_FUNC_ADD);

		GL46.glNamedFramebufferTexture(fbo, GL46.GL_DEPTH_ATTACHMENT, mainRenderTarget.getDepthTextureId(), 0);

		GL46.glNamedFramebufferDrawBuffers(fbo, new int[]{GL46.GL_COLOR_ATTACHMENT0, GL46.GL_COLOR_ATTACHMENT1, GL46.GL_COLOR_ATTACHMENT2});

		GL46.glClearNamedFramebufferfv(fbo, GL46.GL_COLOR, 0, new float[]{0, 0, 0, 0});
		GL46.glClearNamedFramebufferfv(fbo, GL46.GL_COLOR, 1, new float[]{0, 0, 0, 0});
		GL46.glClearNamedFramebufferfv(fbo, GL46.GL_COLOR, 2, new float[]{0, 0, 0, 0});

		GlStateManager._glBindFramebuffer(GL46.GL_FRAMEBUFFER, fbo);
	}

	public void resolveMoments() {
		// No depth writes, but we'll still use the depth test
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
		RenderSystem.blendEquation(GL46.GL_FUNC_ADD);

		Samplers.ZEROTH_MOMENT.makeActive();
		GlStateManager._bindTexture(zerothMoment);

		Samplers.MOMENTS0.makeActive();
		GlStateManager._bindTexture(moments0);

		Samplers.MOMENTS1.makeActive();
		GlStateManager._bindTexture(moments1);

		GL46.glNamedFramebufferDrawBuffers(fbo, new int[]{GL46.GL_COLOR_ATTACHMENT3});

		GL46.glClearNamedFramebufferfv(fbo, GL46.GL_COLOR, 0, new float[]{0, 0, 0, 0});

		GlStateManager._glBindFramebuffer(GL46.GL_FRAMEBUFFER, fbo);
	}

	public void composite() {
		var mainRenderTarget = Minecraft.getInstance()
				.getMainRenderTarget();

		mainRenderTarget.bindWrite(false);

		var oitCompositeProgram = programs.getOitCompositeProgram();

		GlStateManager._depthMask(false);
		GlStateManager._depthFunc(GL46.GL_ALWAYS);
		GlStateManager._enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.DestFactor.SRC_ALPHA);

		oitCompositeProgram.bind();

		GlTextureUnit.T0.makeActive();
		GlStateManager._bindTexture(zerothMoment);

		GlTextureUnit.T1.makeActive();
		GlStateManager._bindTexture(accumulate);

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
		GL46.glDeleteTextures(zerothMoment);
		GL46.glDeleteTextures(moments0);
		GL46.glDeleteTextures(moments1);
		GL46.glDeleteTextures(accumulate);
	}

	private void createTextures(int width, int height) {
		if (lastWidth == width && lastHeight == height) {
			return;
		}

		lastWidth = width;
		lastHeight = height;

		deleteTextures();

		zerothMoment = GL46.glCreateTextures(GL46.GL_TEXTURE_2D);
		moments0 = GL46.glCreateTextures(GL46.GL_TEXTURE_2D);
		moments1 = GL46.glCreateTextures(GL46.GL_TEXTURE_2D);
		accumulate = GL46.glCreateTextures(GL46.GL_TEXTURE_2D);

		GL46.glTextureStorage2D(zerothMoment, 1, GL32.GL_R16F, width, height);
		GL46.glTextureStorage2D(moments0, 1, GL32.GL_RGBA16F, width, height);
		GL46.glTextureStorage2D(moments1, 1, GL32.GL_RGBA16F, width, height);

		GL46.glTextureStorage2D(accumulate, 1, GL32.GL_RGBA16F, width, height);

		//		for (int tex : new int[]{zerothMoment, moments, composite}) {
		//			GL46.glTextureParameteri(tex, GL32.GL_TEXTURE_MIN_FILTER, GL32.GL_NEAREST);
		//			GL46.glTextureParameteri(tex, GL32.GL_TEXTURE_MAG_FILTER, GL32.GL_NEAREST);
		//			GL46.glTextureParameteri(tex, GL32.GL_TEXTURE_COMPARE_MODE, GL32.GL_NONE);
		//			GL46.glTextureParameteri(tex, GL32.GL_TEXTURE_WRAP_S, GL32.GL_CLAMP_TO_EDGE);
		//			GL46.glTextureParameteri(tex, GL32.GL_TEXTURE_WRAP_T, GL32.GL_CLAMP_TO_EDGE);
		//		}

		GL46.glNamedFramebufferTexture(fbo, GL46.GL_COLOR_ATTACHMENT0, zerothMoment, 0);
		GL46.glNamedFramebufferTexture(fbo, GL46.GL_COLOR_ATTACHMENT1, moments0, 0);
		GL46.glNamedFramebufferTexture(fbo, GL46.GL_COLOR_ATTACHMENT2, moments1, 0);
		GL46.glNamedFramebufferTexture(fbo, GL46.GL_COLOR_ATTACHMENT3, accumulate, 0);
	}

	float circleToParameter(float angle) {
		float x = Mth.cos(angle);
		float y = Mth.sin(angle);
		float result = Mth.abs(y) - Mth.abs(x);
		result = (x < 0.0f) ? (2.0f - result) : result;
		result = (y < 0.0f) ? (6.0f - result) : result;
		result += (angle >= 2.0f * Mth.PI) ? 8.0f : 0.0f;
		return result;
	}

	void computeWrappingZoneParameters(float[] out) {
		computeWrappingZoneParameters(out, 0.1f * Mth.PI);
	}

	/*! Given an angle in radians providing the size of the wrapping zone, this
		function computes all constants required by the shader.*/
	void computeWrappingZoneParameters(float[] p_out_wrapping_zone_parameters, float new_wrapping_zone_angle) {
		p_out_wrapping_zone_parameters[0] = new_wrapping_zone_angle;
		p_out_wrapping_zone_parameters[1] = Mth.PI - 0.5f * new_wrapping_zone_angle;
		if (new_wrapping_zone_angle <= 0.0f) {
			p_out_wrapping_zone_parameters[2] = 0.0f;
			p_out_wrapping_zone_parameters[3] = 0.0f;
		} else {
			float zone_end_parameter = 7;
			float zone_begin_parameter = circleToParameter(2.0f * Mth.PI - new_wrapping_zone_angle);
			p_out_wrapping_zone_parameters[2] = 1.0f / (zone_end_parameter - zone_begin_parameter);
			p_out_wrapping_zone_parameters[3] = 1.0f - zone_end_parameter * p_out_wrapping_zone_parameters[2];
		}
	}
}
