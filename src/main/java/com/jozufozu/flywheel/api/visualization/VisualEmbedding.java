package com.jozufozu.flywheel.api.visualization;

import org.joml.Matrix3fc;
import org.joml.Matrix4fc;

import com.jozufozu.flywheel.api.BackendImplemented;

@BackendImplemented
public interface VisualEmbedding extends VisualizationContext {
	/**
	 * Set the transformation matrices for the embedding.
	 *
	 * @param pose   The model matrix.
	 * @param normal The normal matrix.
	 */
	void transforms(Matrix4fc pose, Matrix3fc normal);
}
