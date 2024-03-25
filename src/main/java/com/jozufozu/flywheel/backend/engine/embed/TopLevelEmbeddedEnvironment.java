package com.jozufozu.flywheel.backend.engine.embed;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.backend.Samplers;
import com.jozufozu.flywheel.backend.engine.EngineImpl;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

import net.minecraft.world.level.BlockAndTintGetter;

public class TopLevelEmbeddedEnvironment extends AbstractEmbeddedEnvironment {
	private final EmbeddedLightVolume lightVolume = new EmbeddedLightVolume();
	private final EmbeddedLightTexture lightTexture = new EmbeddedLightTexture();

	public TopLevelEmbeddedEnvironment(EngineImpl engine, RenderStage renderStage) {
		super(engine, renderStage);
	}

	@Override
	public void flush() {
		super.flush();

		if (lightVolume.empty()) {
			return;
		}
		Samplers.EMBEDDED_LIGHT.makeActive();

		lightTexture.bind();

		lightTexture.ensureCapacity(lightVolume.sizeX(), lightVolume.sizeY(), lightVolume.sizeZ());

		lightTexture.upload(lightVolume.ptr(), lightVolume.sizeX(), lightVolume.sizeY(), lightVolume.sizeZ());
	}

	@Override
	public void collectLight(BlockAndTintGetter level, int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ) {
		lightVolume.collect(level, minX, minY, minZ, sizeX, sizeY, sizeZ);
	}

	@Override
	public void invalidateLight() {
		lightVolume.clear();
	}

	@Override
	public void setupLight(GlProgram program) {
		if (!lightVolume.empty()) {
			Samplers.EMBEDDED_LIGHT.makeActive();

			lightTexture.bind();

			float oneOverSizeX = 1f / (float) lightTexture.sizeX;
			float oneOverSizeY = 1f / (float) lightTexture.sizeY;
			float oneOverSizeZ = 1f / (float) lightTexture.sizeZ;

			program.setVec3(EmbeddingUniforms.ONE_OVER_LIGHT_BOX_SIZE, oneOverSizeX, oneOverSizeY, oneOverSizeZ);
			program.setVec3(EmbeddingUniforms.LIGHT_VOLUME_MIN, lightVolume.x(), lightVolume.y(), lightVolume.z());
			program.setBool(EmbeddingUniforms.USE_LIGHT_VOLUME, true);
		} else {
			program.setBool(EmbeddingUniforms.USE_LIGHT_VOLUME, false);
		}
	}

	@Override
	public void composeMatrices(Matrix4f pose, Matrix3f normal) {
		pose.set(this.pose);
		normal.set(this.normal);
	}

	@Override
	public void actuallyDelete() {
		// We could technically free the light volume right away in _delete, but
		// the control flow here is so convoluted that it's probably best to do
		// everything in one place.
		lightVolume.delete();
		lightTexture.delete();
	}
}
