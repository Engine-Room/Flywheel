package com.jozufozu.flywheel.impl.extension;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public interface LevelExtension {
	/**
	 * Get an iterator over all entities in this level.
	 *
	 * <p>
	 *     Normally, this would be accomplished by {@link ClientLevel#entitiesForRendering}, but the output of that
	 *     method does not include entities that are rendered by Flywheel. This interface provides a workaround.
	 * </p>
	 * @return An iterator over all entities in the level, including entities that are rendered by Flywheel.
	 */
	Iterable<Entity> flywheel$getAllLoadedEntities();

	static Iterable<Entity> getAllLoadedEntities(Level level) {
		return ((LevelExtension) level).flywheel$getAllLoadedEntities();
	}
}
