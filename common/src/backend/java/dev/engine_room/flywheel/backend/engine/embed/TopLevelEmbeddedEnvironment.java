package dev.engine_room.flywheel.backend.engine.embed;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import dev.engine_room.flywheel.api.event.RenderStage;
import dev.engine_room.flywheel.backend.engine.EngineImpl;

public class TopLevelEmbeddedEnvironment extends AbstractEmbeddedEnvironment {
	public TopLevelEmbeddedEnvironment(EngineImpl engine, RenderStage renderStage) {
		super(engine, renderStage);
	}

	@Override
	public void composeMatrices(Matrix4f pose, Matrix3f normal) {
		pose.set(this.pose);
		normal.set(this.normal);
	}
}
