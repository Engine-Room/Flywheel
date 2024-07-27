package dev.engine_room.flywheel.api.visualization;

import org.joml.Matrix3fc;
import org.joml.Matrix4fc;

import dev.engine_room.flywheel.api.backend.BackendImplemented;

/**
 * A visualization context that can apply a transformation to instances created through its instancer provider.
 *
 * <p>This is intended to be used for large meta-visuals that may be composed of many block entities or entities.
 * Invoking a visualizer with a VisualEmbedding will create a "subvisual". The parent visual is responsible for managing
 * the lifecycle of subvisuals: deleting them, and optionally invoking their frame and tick plans. Subvisuals exist in
 * the real world from their perspective, and in general visuals should not care if they are within a VisualEmbedding.
 * However, if a visual wants to check it can use {@code instanceof VisualEmbedding} on its VisualizationContext.</p>
 */
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
