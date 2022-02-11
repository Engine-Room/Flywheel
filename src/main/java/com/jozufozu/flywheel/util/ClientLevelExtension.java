package com.jozufozu.flywheel.util;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

public interface ClientLevelExtension {

	/**
	 * Get an iterator over all entities in this level.
	 *
	 * <p>
	 *     Normally, this would be accomplished by {@link ClientLevel#entitiesForRendering()}, but the output of that
	 *     method is filtered of entities that are rendered by flywheel. This interface provides a workaround.
	 * </p>
	 * @return An iterator over all entities in the level, including entities that are rendered by flywheel.
	 */
	Iterable<Entity> flywheel$getAllLoadedEntities();

	static ClientLevelExtension cast(ClientLevel level) {
		return (ClientLevelExtension) level;
	}
}
