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
import com.jozufozu.flywheel.backend.compile.ContextShader;
import com.jozufozu.flywheel.backend.engine.EngineImpl;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.util.AtomicReferenceCounted;

import net.minecraft.core.Vec3i;

public abstract class AbstractEmbeddedEnvironment extends AtomicReferenceCounted implements Environment, VisualEmbedding {
	protected final Matrix4f pose = new Matrix4f();
	protected final Matrix3f normal = new Matrix3f();
	private final Matrix4f poseComposed = new Matrix4f();
	private final Matrix3f normalComposed = new Matrix3f();
	private final InstancerProvider instancerProvider;
	private final EngineImpl engine;
	private final RenderStage renderStage;

	public AbstractEmbeddedEnvironment(EngineImpl engine, RenderStage renderStage) {
		this.engine = engine;
		this.renderStage = renderStage;

		instancerProvider = new InstancerProvider() {
			@Override
			public <I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model) {
				// Kinda cursed usage of anonymous classes here, but it does the job.
				return engine.instancer(AbstractEmbeddedEnvironment.this, type, model, renderStage);
			}
		};

		// Acquire the reference owned by the visual that created this.
		acquire();
	}

	@Override
	public void transforms(Matrix4fc pose, Matrix3fc normal) {
		this.pose.set(pose);
		this.normal.set(normal);
	}

	public void flush() {
		poseComposed.identity();
		normalComposed.identity();

		composeMatrices(poseComposed, normalComposed);
	}

	@Override
	public void setupDraw(GlProgram program) {
		setupLight(program);

		program.setMat4(EmbeddingUniforms.MODEL_MATRIX, poseComposed);
		program.setMat3(EmbeddingUniforms.NORMAL_MATRIX, normalComposed);
	}

	@Override
	public void setupCull(GlProgram program) {
		program.setBool(EmbeddingUniforms.USE_MODEL_MATRIX, true);

		program.setMat4(EmbeddingUniforms.MODEL_MATRIX1, poseComposed);
	}

	@Override
	public ContextShader contextShader() {
		return ContextShader.EMBEDDED;
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
		var out = new NestedEmbeddedEnvironment(this, engine, renderStage);
		engine.environmentStorage()
				.track(out);
		return out;
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
		engine.environmentStorage().enqueueDeletion(this);
	}

	public abstract void setupLight(GlProgram program);

	public abstract void composeMatrices(Matrix4f pose, Matrix3f normal);

	/**
	 * Called in EnvironmentStorage#flush
	 */
	public abstract void actuallyDelete();
}
