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
import com.jozufozu.flywheel.backend.engine.EngineImpl;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.util.AtomicReferenceCounted;

import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockAndTintGetter;

public class EmbeddedEnvironment extends AtomicReferenceCounted implements Environment, VisualEmbedding {
	private final Matrix4f pose = new Matrix4f();
	private final Matrix3f normal = new Matrix3f();

	private final EmbeddedLightVolume lightVolume = new EmbeddedLightVolume();
	private final EmbeddedLightTexture lightTexture = new EmbeddedLightTexture();

	private final InstancerProvider instancerProvider;
	private final EngineImpl engine;
	private final RenderStage renderStage;

	public EmbeddedEnvironment(EngineImpl engine, RenderStage renderStage) {
		this.engine = engine;
		this.renderStage = renderStage;

		instancerProvider = new InstancerProvider() {
			@Override
			public <I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model) {
				// Kinda cursed usage of anonymous classes here, but it does the job.
				return engine.instancer(EmbeddedEnvironment.this, type, model, renderStage);
			}
		};

		// Acquire the reference owned by the visual that created this.
		acquire();
	}

	public void flush() {
		if (lightVolume.empty()) {
			return;
		}
		Samplers.EMBEDDED_LIGHT.makeActive();

		lightTexture.bind();

		lightTexture.ensureCapacity(lightVolume.sizeX(), lightVolume.sizeY(), lightVolume.sizeZ());

		lightTexture.upload(lightVolume.ptr(), lightVolume.sizeX(), lightVolume.sizeY(), lightVolume.sizeZ());
	}

	@Override
	public void transforms(Matrix4fc pose, Matrix3fc normal) {
		this.pose.set(pose);
		this.normal.set(normal);
	}

	@Override
	public void collectLight(BlockAndTintGetter level, int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ) {
		lightVolume.collect(level, minX, minY, minZ, sizeX, sizeY, sizeZ);
	}

	@Override
	public void invalidateLight() {
		lightVolume.delete();
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

			float oneOverSizeX = 1f / (float) lightTexture.sizeX;
			float oneOverSizeY = 1f / (float) lightTexture.sizeY;
			float oneOverSizeZ = 1f / (float) lightTexture.sizeZ;

			drawProgram.setVec3("_flw_oneOverLightBoxSize", oneOverSizeX, oneOverSizeY, oneOverSizeZ);
			drawProgram.setVec3("_flw_lightVolumeMin", lightVolume.x(), lightVolume.y(), lightVolume.z());
			drawProgram.setBool("_flw_useLightVolume", true);
		} else {
			drawProgram.setBool("_flw_useLightVolume", false);
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
		return engine.createEmbedding(renderStage);
	}

	/**
	 * Called by visuals
	 */
	@Override
	public void delete() {
		// Release the reference owned by the visual that created this.
		// Note that visuals don't explicitly call acquire, instead the
		// storage acquired a reference when this was constructed.
		release();
	}

	/**
	 * Called when referenceCount goes to 0
	 */
	@Override
	public void _delete() {
		engine.freeEmbedding(this);
	}

	/**
	 * Called in EnvironmentStorage#flush
	 */
	public void actuallyDelete() {
		// We could technically free the light volume right away in _delete, but
		// the control flow here is so convoluted that it's probably best to do
		// everything in one place.
		lightVolume.delete();
		lightTexture.delete();
	}
}
