package dev.engine_room.flywheel.api.visualization;

import org.joml.Matrix3fc;
import org.joml.Matrix4fc;

import dev.engine_room.flywheel.api.backend.BackendImplemented;

@BackendImplemented
public interface VisualEmbedding extends VisualizationContext {
	/**
	 * Set the transformation matrices for the embedding.
	 *
	 * @param pose   The model matrix.
	 * @param normal The normal matrix.
	 */
	void transforms(Matrix4fc pose, Matrix3fc normal);

	/**
	 * Delete this embedding.
	 *
	 * <p>After this method exits, the embedding will continue to function in the state it was in before
	 * this method was called. Once all child instancers are deleted, the resources owned by this embedding
	 * will be freed. Creating new instancers after calling this method will throw an error.</p>
	 */
	void delete();
}
