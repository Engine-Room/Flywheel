package com.jozufozu.flywheel.core.shader;

import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform3f;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.mojang.blaze3d.platform.Window;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;

public class WorldProgram extends GlProgram {
	protected final int uTime = getUniformLocation("uTime");
	protected final int uViewProjection = getUniformLocation("uViewProjection");
	protected final int uCameraPos = getUniformLocation("uCameraPos");
	protected final int uWindowSize = getUniformLocation("uWindowSize");
	protected final int uConstantAmbientLight = getUniformLocation("uConstantAmbientLight");
	private final WorldFog fog;

	protected int uBlockAtlas;
	protected int uLightMap;

	public WorldProgram(ResourceLocation name, int handle) {
		super(name, handle);

		fog = new WorldFog(this);

		bind();
		registerSamplers();
		unbind();
	}

	protected void registerSamplers() {
		uBlockAtlas = setSamplerBinding("uBlockAtlas", 0);
		uLightMap = setSamplerBinding("uLightMap", 2);
	}

	public void uploadUniforms(double camX, double camY, double camZ, Matrix4f viewProjection, ClientLevel level) {
		fog.uploadUniforms();
		uploadTime(AnimationTickHolder.getRenderTime());
		uploadViewProjection(viewProjection);
		uploadCameraPos(camX, camY, camZ);
		uploadWindowSize();
		uploadConstantAmbientLight(level);
	}

	protected void uploadTime(float renderTime) {
		if (uTime < 0) return;

		glUniform1f(uTime, renderTime);
	}

	protected void uploadViewProjection(Matrix4f viewProjection) {
		if (uViewProjection < 0) return;

		uploadMatrixUniform(uViewProjection, viewProjection);
	}

	protected void uploadCameraPos(double camX, double camY, double camZ) {
		if (uCameraPos < 0) return;

		glUniform3f(uCameraPos, (float) camX, (float) camY, (float) camZ);
	}

	protected void uploadWindowSize() {
		if (uWindowSize < 0) return;

		Window window = Minecraft.getInstance().getWindow();

		int height = window.getScreenHeight();
		int width = window.getScreenWidth();
		glUniform2f(uWindowSize, width, height);
	}

	protected void uploadConstantAmbientLight(ClientLevel level) {
		if (uConstantAmbientLight < 0) return;

		glUniform1i(uConstantAmbientLight, level.effects().constantAmbientLight() ? 1 : 0);
	}
}
