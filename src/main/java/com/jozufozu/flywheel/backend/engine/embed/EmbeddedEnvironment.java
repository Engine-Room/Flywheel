package com.jozufozu.flywheel.backend.engine.embed;

import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.instance.InstancerProvider;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visualization.VisualEmbedding;
import com.jozufozu.flywheel.backend.Samplers;
import com.jozufozu.flywheel.backend.compile.ContextShader;
import com.jozufozu.flywheel.backend.engine.AbstractEngine;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockAndTintGetter;

public class EmbeddedEnvironment implements Environment, VisualEmbedding {
	private final Matrix4f pose = new Matrix4f();
	private final Matrix3f normal = new Matrix3f();

	private final EmbeddedLightVolume lightVolume = new EmbeddedLightVolume();
	private final EmbeddedLightTexture lightTexture = new EmbeddedLightTexture();

	private final InstancerProvider instancerProvider;
	private final AbstractEngine engine;
	private final RenderStage renderStage;

	public EmbeddedEnvironment(AbstractEngine engine, RenderStage renderStage) {
		this.engine = engine;
		this.renderStage = renderStage;

		instancerProvider = new InstancerProvider() {
			@Override
			public <I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model) {
				// Kinda cursed usage of anonymous classes here, but it does the job.
				return engine.instancer(EmbeddedEnvironment.this, type, model, renderStage);
			}
		};
	}

	@Override
	public void transforms(Matrix4fc pose, Matrix3fc normal) {
		this.pose.set(pose);
		this.normal.set(normal);
	}

	@Override
	public void light(BlockAndTintGetter level, int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ) {
		lightVolume.collect(level, minX, minY, minZ, sizeX, sizeY, sizeZ);
	}

	@Override
	public void invalidateLight(int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ) {
		lightVolume.invalidate(minX, minY, minZ, sizeX, sizeY, sizeZ);
	}

	@Override
	public ContextShader contextShader() {
		return ContextShader.EMBEDDED;
	}

	@Override
	public void setupDraw(GlProgram drawProgram) {
		if (!lightVolume.empty()) {
			Samplers.EMBEDDED_LIGHT.makeActive();

			lightTexture.bind();

			lightTexture.ensureCapacity(lightVolume.sizeX, lightVolume.sizeY, lightVolume.sizeZ);

			lightTexture.upload(lightVolume.ptr(), lightVolume.sizeX, lightVolume.sizeY, lightVolume.sizeZ);

			float oneOverSizeX = 1f / (float) lightTexture.sizeX;
			float oneOverSizeY = 1f / (float) lightTexture.sizeY;
			float oneOverSizeZ = 1f / (float) lightTexture.sizeZ;

			drawProgram.setVec3("_flw_oneOverLightBoxSize", oneOverSizeX, oneOverSizeY, oneOverSizeZ);
			drawProgram.setVec3("_flw_lightVolumeMin", lightVolume.minX, lightVolume.minY, lightVolume.minZ);
		}
		drawProgram.setMat4("_flw_model", pose);
		drawProgram.setMat3("_flw_normal", normal);
	}

	@Override
	public void setupCull(GlProgram cullProgram) {
		cullProgram.setBool("_flw_useEmbeddedModel", true);
		cullProgram.setMat4("_flw_embeddedModel", pose);
	}

	@Override
	public InstancerProvider instancerProvider() {
		return instancerProvider;
	}

	@Override
	public Vec3i renderOrigin() {
		return Vec3i.ZERO;
	}

	@Override
	public VisualEmbedding createEmbedding() {
		return new EmbeddedEnvironment(engine, renderStage);
	}

	@Override
	public void delete() {
		lightVolume.delete();
		lightTexture.delete();
	}
}
