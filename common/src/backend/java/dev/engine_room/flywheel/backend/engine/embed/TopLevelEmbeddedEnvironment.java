package dev.engine_room.flywheel.backend.engine.embed;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import dev.engine_room.flywheel.api.event.RenderStage;
import dev.engine_room.flywheel.backend.engine.EngineImpl;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;

public class TopLevelEmbeddedEnvironment extends AbstractEmbeddedEnvironment {
	private final LongSet lightSections = new LongArraySet();

	public TopLevelEmbeddedEnvironment(EngineImpl engine, RenderStage renderStage) {
		super(engine, renderStage);
	}

	@Override
	public void flush() {
		super.flush();

		lightSections.forEach(engine.lightStorage()::addSection);
	}

	@Override
	public void lightChunks(LongSet chunks) {
		lightSections.clear();
		lightSections.addAll(chunks);
	}

	@Override
	public void setupLight(GlProgram program) {
		program.setBool(EmbeddingUniforms.USE_LIGHT_VOLUME, !lightSections.isEmpty());
	}

	@Override
	public void composeMatrices(Matrix4f pose, Matrix3f normal) {
		pose.set(this.pose);
		normal.set(this.normal);
	}

	@Override
	protected void _delete() {

	}
}
