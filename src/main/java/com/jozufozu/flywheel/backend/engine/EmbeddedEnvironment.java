package com.jozufozu.flywheel.backend.engine;

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
import com.jozufozu.flywheel.backend.compile.ContextShaders;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

import net.minecraft.core.Vec3i;

public class EmbeddedEnvironment implements Environment, VisualEmbedding {
	private final Matrix4f pose = new Matrix4f();
	private final Matrix3f normal = new Matrix3f();

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
	public ContextShader contextShader() {
		return ContextShaders.EMBEDDED;
	}

	@Override
	public void setupDraw(GlProgram drawProgram) {
		drawProgram.setVec3("_flw_oneOverLightBoxSize", 1, 1, 1);
		drawProgram.setVec3("_flw_lightVolumeMin", 0, 0, 0);
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
}
