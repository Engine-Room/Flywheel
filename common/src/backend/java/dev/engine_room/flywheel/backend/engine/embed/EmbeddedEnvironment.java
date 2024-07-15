package dev.engine_room.flywheel.backend.engine.embed;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import dev.engine_room.flywheel.api.event.RenderStage;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualEmbedding;
import dev.engine_room.flywheel.backend.compile.ContextShader;
import dev.engine_room.flywheel.backend.engine.EngineImpl;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import net.minecraft.core.Vec3i;

public class EmbeddedEnvironment implements VisualEmbedding, Environment {
	private final EngineImpl engine;
	private final RenderStage renderStage;
	@Nullable
	private final EmbeddedEnvironment parent;
	private final InstancerProvider instancerProvider;

	private final Matrix4f pose = new Matrix4f();
	private final Matrix3f normal = new Matrix3f();
	private final Matrix4f poseComposed = new Matrix4f();
	private final Matrix3f normalComposed = new Matrix3f();

	private boolean deleted = false;

	public EmbeddedEnvironment(EngineImpl engine, RenderStage renderStage, @Nullable EmbeddedEnvironment parent) {
		this.engine = engine;
		this.renderStage = renderStage;
		this.parent = parent;

		instancerProvider = new InstancerProvider() {
			@Override
			public <I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model) {
				// Kinda cursed usage of anonymous classes here, but it does the job.
				return engine.instancer(EmbeddedEnvironment.this, type, model, renderStage);
			}
		};
	}

	public EmbeddedEnvironment(EngineImpl engine, RenderStage renderStage) {
		this(engine, renderStage, null);
	}

	@Override
	public void transforms(Matrix4fc pose, Matrix3fc normal) {
		this.pose.set(pose);
		this.normal.set(normal);
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
		var out = new EmbeddedEnvironment(engine, renderStage, this);
		engine.environmentStorage()
				.track(out);
		return out;
	}

	@Override
	public ContextShader contextShader() {
		return ContextShader.EMBEDDED;
	}

	@Override
	public void setupCull(GlProgram program) {
		program.setBool(EmbeddingUniforms.USE_MODEL_MATRIX, true);
		program.setMat4(EmbeddingUniforms.MODEL_MATRIX, poseComposed);
	}

	@Override
	public void setupDraw(GlProgram program) {
		program.setMat4(EmbeddingUniforms.MODEL_MATRIX, poseComposed);
		program.setMat3(EmbeddingUniforms.NORMAL_MATRIX, normalComposed);
	}

	public void flush() {
		poseComposed.identity();
		normalComposed.identity();

		composeMatrices(poseComposed, normalComposed);
	}

	private void composeMatrices(Matrix4f pose, Matrix3f normal) {
		if (parent != null) {
			parent.composeMatrices(pose, normal);
			pose.mul(this.pose);
			normal.mul(this.normal);
		} else {
			pose.set(this.pose);
			normal.set(this.normal);
		}
	}

	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * Called by visuals
	 */
	@Override
	public void delete() {
		deleted = true;
	}
}
