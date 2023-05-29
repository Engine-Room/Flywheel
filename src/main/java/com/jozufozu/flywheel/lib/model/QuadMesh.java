package com.jozufozu.flywheel.lib.model;

import com.jozufozu.flywheel.api.model.IndexSequence;
import com.jozufozu.flywheel.api.model.Mesh;

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
