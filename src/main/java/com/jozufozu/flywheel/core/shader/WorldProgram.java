package com.jozufozu.flywheel.core.shader;

import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform3f;

import com.jozufozu.flywheel.core.shader.extension.WorldFog;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.mojang.blaze3d.platform.Window;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class WorldProgram extends ExtensibleGlProgram {
	protected final int uTime = getUniformLocation("uTime");
	protected final int uViewProjection = getUniformLocation("uViewProjection");
	protected final int uCameraPos = getUniformLocation("uCameraPos");
	protected final int uWindowSize = getUniformLocation("uWindowSize");

	protected int uBlockAtlas;
	protected int uLightMap;

	public WorldProgram(ResourceLocation name, int handle) {
		super(name, handle);

		this.extensions.add(new WorldFog(this));

		super.bind();
		registerSamplers();
		super.unbind();
	}

	protected void registerSamplers() {
		uBlockAtlas = setSamplerBinding("uBlockAtlas", 0);
		uLightMap = setSamplerBinding("uLightMap", 2);
	}

	public void uploadViewProjection(Matrix4f viewProjection) {
		if (uViewProjection < 0) return;

		uploadMatrixUniform(uViewProjection, viewProjection);
	}

	public void uploadWindowSize() {
		if (uWindowSize < 0) return;

		Window window = Minecraft.getInstance().getWindow();

		int height = window.getScreenHeight();
		int width = window.getScreenWidth();
		glUniform2f(uWindowSize, width, height);
	}

	public void uploadCameraPos(double camX, double camY, double camZ) {
		if (uCameraPos < 0) return;

		glUniform3f(uCameraPos, (float) camX, (float) camY, (float) camZ);
	}

	public void uploadTime(float renderTime) {
		if (uTime < 0) return;

		glUniform1f(uTime, renderTime);
	}

	@Override
	public void bind() {
		super.bind();

		uploadWindowSize();
		uploadTime(AnimationTickHolder.getRenderTime());
	}
}
