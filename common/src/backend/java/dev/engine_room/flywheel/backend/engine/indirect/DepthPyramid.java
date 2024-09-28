package dev.engine_room.flywheel.backend.engine.indirect;

import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL46;

import com.mojang.blaze3d.platform.GlStateManager;

import dev.engine_room.flywheel.backend.compile.IndirectPrograms;
import dev.engine_room.flywheel.backend.gl.GlTextureUnit;
import dev.engine_room.flywheel.lib.math.MoreMath;
import net.minecraft.client.Minecraft;

public class DepthPyramid {
	private final IndirectPrograms programs;

	public int pyramidTextureId = -1;

	private int lastWidth = -1;
	private int lastHeight = -1;

	public DepthPyramid(IndirectPrograms programs) {
		this.programs = programs;
	}

	public void generate() {
		var mainRenderTarget = Minecraft.getInstance()
				.getMainRenderTarget();

		int width = mip0Size(mainRenderTarget.width);
		int height = mip0Size(mainRenderTarget.height);

		int mipLevels = getImageMipLevels(width, height);

		createPyramidMips(mipLevels, width, height);

		int depthBufferId = mainRenderTarget.getDepthTextureId();

		GL46.glMemoryBarrier(GL46.GL_FRAMEBUFFER_BARRIER_BIT);

		GlTextureUnit.T0.makeActive();
		GlStateManager._bindTexture(depthBufferId);

		var downsampleFirstProgram = programs.getDownsampleFirstProgram();
		downsampleFirstProgram.bind();
		downsampleFirstProgram.setUInt("max_mip_level", mipLevels);

		for (int i = 0; i < Math.min(6, mipLevels); i++) {
			GL46.glBindImageTexture(i + 1, pyramidTextureId, i, false, 0, GL32.GL_WRITE_ONLY, GL32.GL_R32F);
		}

		GL46.glDispatchCompute(MoreMath.ceilingDiv(width << 1, 64), MoreMath.ceilingDiv(height << 1, 64), 1);

		if (mipLevels < 7) {
			GL46.glMemoryBarrier(GL46.GL_TEXTURE_FETCH_BARRIER_BIT);

			return;
		}

		GL46.glMemoryBarrier(GL46.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

		var downsampleSecondProgram = programs.getDownsampleSecondProgram();
		downsampleSecondProgram.bind();
		downsampleSecondProgram.setUInt("max_mip_level", mipLevels);

		// Note: mip_6 in the shader is actually mip level 5 in the texture
		GL46.glBindImageTexture(0, pyramidTextureId, 5, false, 0, GL32.GL_READ_ONLY, GL32.GL_R32F);
		for (int i = 6; i < Math.min(12, mipLevels); i++) {
			GL46.glBindImageTexture(i - 5, pyramidTextureId, i, false, 0, GL32.GL_WRITE_ONLY, GL32.GL_R32F);
		}

		GL46.glDispatchCompute(1, 1, 1);

		GL46.glMemoryBarrier(GL46.GL_TEXTURE_FETCH_BARRIER_BIT);
	}

	public void bindForCull() {
		GlTextureUnit.T0.makeActive();
		GlStateManager._bindTexture(pyramidTextureId);
	}

	public void delete() {
		if (pyramidTextureId != -1) {
			GL32.glDeleteTextures(pyramidTextureId);
			pyramidTextureId = -1;
		}
	}

	private void createPyramidMips(int mipLevels, int width, int height) {
		if (lastWidth == width && lastHeight == height) {
			return;
		}

		lastWidth = width;
		lastHeight = height;

		delete();

		pyramidTextureId = GL46.glCreateTextures(GL46.GL_TEXTURE_2D);
		GL46.glTextureStorage2D(pyramidTextureId, mipLevels, GL32.GL_R32F, width, height);

		GL46.glTextureParameteri(pyramidTextureId, GL32.GL_TEXTURE_MIN_FILTER, GL32.GL_NEAREST);
		GL46.glTextureParameteri(pyramidTextureId, GL32.GL_TEXTURE_MAG_FILTER, GL32.GL_NEAREST);
		GL46.glTextureParameteri(pyramidTextureId, GL32.GL_TEXTURE_COMPARE_MODE, GL32.GL_NONE);
		GL46.glTextureParameteri(pyramidTextureId, GL32.GL_TEXTURE_WRAP_S, GL32.GL_CLAMP_TO_EDGE);
		GL46.glTextureParameteri(pyramidTextureId, GL32.GL_TEXTURE_WRAP_T, GL32.GL_CLAMP_TO_EDGE);
	}

	public static int mipSize(int mip0Size, int level) {
		return Math.max(1, mip0Size >> level);
	}

	public static int mip0Size(int screenSize) {
		return Integer.highestOneBit(screenSize);
	}

	public static int getImageMipLevels(int width, int height) {
		int result = 1;

		while (width > 2 && height > 2) {
			result++;
			width >>= 1;
			height >>= 1;
		}

		return result;
	}
}
