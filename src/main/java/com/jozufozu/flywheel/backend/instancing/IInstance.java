package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.backend.instancing.tile.TileInstanceManager;

import net.minecraft.util.math.BlockPos;

/**
 * A general interface providing information about any type of thing that could use Flywheel's instanced rendering.
 * Right now, that's only {@link TileInstanceManager}, but there could be an entity equivalent in the future.
 */
public interface IInstance {

	BlockPos getWorldPosition();

	void updateLight();

	void remove();

	/**
	 * When an instance is reset, the instance is deleted and re-created.
	 *
	 * <p>
	 *     This is used to handle things like block state changes.
	 * </p>
	 *
	 * @return true if this instance should be reset
	 */
	boolean shouldReset();

	void update();
}
