package dev.engine_room.flywheel.backend.engine.indirect;

import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL46;

import com.mojang.blaze3d.platform.GlStateManager;

import dev.engine_room.flywheel.backend.gl.GlTextureUnit;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import dev.engine_room.flywheel.lib.math.MoreMath;
import net.minecraft.client.Minecraft;

public class DepthPyramid {
	private final GlProgram depthReduceProgram;

	public final int pyramidTextureId;

	private int lastWidth = -1;
	private int lastHeight = -1;

	public DepthPyramid(GlProgram depthReduceProgram) {
		this.depthReduceProgram = depthReduceProgram;

		pyramidTextureId = GL32.glGenTextures();

		GlStateManager._bindTexture(pyramidTextureId);
		GlStateManager._texParameter(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_MIN_FILTER, GL32.GL_NEAREST);
		GlStateManager._texParameter(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_MAG_FILTER, GL32.GL_NEAREST);
		GlStateManager._texParameter(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_COMPARE_MODE, GL32.GL_NONE);
		GlStateManager._texParameter(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_WRAP_S, GL32.GL_CLAMP_TO_EDGE);
		GlStateManager._texParameter(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_WRAP_T, GL32.GL_CLAMP_TO_EDGE);

	}

	public void generate() {
		var mainRenderTarget = Minecraft.getInstance()
				.getMainRenderTarget();

		int width = mip0Size(mainRenderTarget.width);
		int height = mip0Size(mainRenderTarget.height);

		int mipLevels = getImageMipLevels(width, height);

		createPyramidMips(mipLevels, width, height);

		int depthBufferId = mainRenderTarget.getDepthTextureId();

		GlTextureUnit.T1.makeActive();
		GlStateManager._bindTexture(depthBufferId);

		GL46.glMemoryBarrier(GL46.GL_FRAMEBUFFER_BARRIER_BIT);

		GL46.glActiveTexture(GL32.GL_TEXTURE1);

		depthReduceProgram.bind();

		for (int i = 0; i < mipLevels; i++) {
			int mipWidth = mipSize(width, i);
			int mipHeight = mipSize(height, i);

			int srcTexture = (i == 0) ? depthBufferId : pyramidTextureId;
			GlStateManager._bindTexture(srcTexture);

			GL46.glBindImageTexture(0, pyramidTextureId, i, false, 0, GL32.GL_WRITE_ONLY, GL32.GL_R32F);

			depthReduceProgram.setVec2("imageSize", mipWidth, mipHeight);
			depthReduceProgram.setInt("lod", Math.max(0, i - 1));

			GL46.glDispatchCompute(MoreMath.ceilingDiv(mipWidth, 8), MoreMath.ceilingDiv(mipHeight, 8), 1);

			GL46.glMemoryBarrier(GL46.GL_TEXTURE_FETCH_BARRIER_BIT);
		}
	}

	public void delete() {
		GL32.glDeleteTextures(pyramidTextureId);
	}

	private void createPyramidMips(int mipLevels, int width, int height) {
		if (lastWidth == width && lastHeight == height) {
			return;
		}

		lastWidth = width;
		lastHeight = height;

		GL32.glBindTexture(GL32.GL_TEXTURE_2D, pyramidTextureId);

		for (int i = 0; i < mipLevels; i++) {
			int mipWidth = mipSize(width, i);
			int mipHeight = mipSize(height, i);

			GL32.glTexImage2D(GL32.GL_TEXTURE_2D, i, GL32.GL_R32F, mipWidth, mipHeight, 0, GL32.GL_RED, GL32.GL_FLOAT, 0);
		}
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
