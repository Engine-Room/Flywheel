package com.jozufozu.flywheel.api.visualization;

import org.joml.Matrix3fc;
import org.joml.Matrix4fc;

import com.jozufozu.flywheel.api.BackendImplemented;

import net.minecraft.world.level.BlockAndTintGetter;

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
	 * Collect light information from the given level for the given box.
	 *
	 * <p>Call this method on as many or as few boxes as you need to
	 * encompass all child visuals of this embedding.</p>
	 *
	 * <p>After this method is called, instances rendered from this
	 * embedding within the given box will be lit as if they were in
	 * the given level.</p>
	 *
	 * @param level The level to collect light information from.
	 * @param minX  The minimum x coordinate of the box.
	 * @param minY  The minimum y coordinate of the box.
	 * @param minZ  The minimum z coordinate of the box.
	 * @param sizeX The size of the box in the x direction.
	 * @param sizeY The size of the box in the y direction.
	 * @param sizeZ The size of the box in the z direction.
	 */
	void light(BlockAndTintGetter level, int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ);

	/**
	 * Reset any collected lighting information for the given box.
	 *
	 * @param minX  The minimum x coordinate of the box.
	 * @param minY  The minimum y coordinate of the box.
	 * @param minZ  The minimum z coordinate of the box.
	 * @param sizeX The size of the box in the x direction.
	 * @param sizeY The size of the box in the y direction.
	 * @param sizeZ The size of the box in the z direction.
	 */
	void invalidateLight(int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ);
}
