package dev.engine_room.flywheel.lib.model;

import dev.engine_room.flywheel.api.model.IndexSequence;
import dev.engine_room.flywheel.api.model.Mesh;

public interface QuadMesh extends Mesh {
	@Override
	default IndexSequence indexSequence() {
		return QuadIndexSequence.INSTANCE;
	}

	@Override
	default int indexCount() {
		return vertexCount() / 2 * 3;
	}
}
