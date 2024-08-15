package dev.engine_room.flywheel.backend.engine.embed;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualEmbedding;
import dev.engine_room.flywheel.api.visualization.VisualType;
import dev.engine_room.flywheel.backend.compile.ContextShader;
import dev.engine_room.flywheel.backend.engine.EngineImpl;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import dev.engine_room.flywheel.lib.util.ExtraMemoryOps;
import net.minecraft.core.Vec3i;

public class EmbeddedEnvironment implements VisualEmbedding, Environment {
	private final EngineImpl engine;
	private final VisualType visualType;
	private final Vec3i renderOrigin;
	@Nullable
	private final EmbeddedEnvironment parent;
	private final InstancerProvider instancerProvider;

	private final Matrix4f pose = new Matrix4f();
	private final Matrix3f normal = new Matrix3f();
	private final Matrix4f poseComposed = new Matrix4f();
	private final Matrix3f normalComposed = new Matrix3f();

	public int matrixIndex = 0;

	private boolean deleted = false;

	public EmbeddedEnvironment(EngineImpl engine, VisualType visualType, Vec3i renderOrigin, @Nullable EmbeddedEnvironment parent) {
		this.engine = engine;
		this.visualType = visualType;
		this.renderOrigin = renderOrigin;
		this.parent = parent;

		instancerProvider = new InstancerProvider() {
			@Override
			public <I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model, int bias) {
				// Kinda cursed usage of anonymous classes here, but it does the job.
				return engine.instancer(EmbeddedEnvironment.this, type, model, visualType, bias);
			}
		};
	}

	public EmbeddedEnvironment(EngineImpl engine, VisualType visualType, Vec3i renderOrigin) {
		this(engine, visualType, renderOrigin, null);
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
		return renderOrigin;
	}

	@Override
	public VisualEmbedding createEmbedding(Vec3i renderOrigin) {
		var out = new EmbeddedEnvironment(engine, visualType, renderOrigin, this);
		engine.environmentStorage()
				.track(out);
		return out;
	}

	@Override
	public ContextShader contextShader() {
		return ContextShader.EMBEDDED;
	}

	@Override
	public void setupDraw(GlProgram program) {
		program.setMat4(EmbeddingUniforms.MODEL_MATRIX, poseComposed);
		program.setMat3(EmbeddingUniforms.NORMAL_MATRIX, normalComposed);
	}

	@Override
	public int matrixIndex() {
		return matrixIndex;
	}

	public void flush(long ptr) {
		poseComposed.identity();
		normalComposed.identity();

		composeMatrices(poseComposed, normalComposed);

		ExtraMemoryOps.putMatrix4f(ptr, poseComposed);
		ExtraMemoryOps.putMatrix3fPadded(ptr + 16 * Float.BYTES, normalComposed);
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
