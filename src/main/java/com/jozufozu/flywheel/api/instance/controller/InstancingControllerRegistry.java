package com.jozufozu.flywheel.api.instance.controller;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.impl.InstancingControllerRegistryImpl;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * A utility class for registering and retrieving {@code InstancingController}s.
 */
public final class InstancingControllerRegistry {
	/**
	 * Gets the instancing controller for the given block entity type, if one exists.
	 * @param type The block entity type to get the instancing controller for.
	 * @param <T> The type of the block entity.
	 * @return The instancing controller for the given block entity type, or {@code null} if none exists.
	 */
	@Nullable
	public static <T extends BlockEntity> BlockEntityInstancingController<? super T> getController(BlockEntityType<T> type) {
		return InstancingControllerRegistryImpl.getController(type);
	}

	/**
	 * Gets the instancing controller for the given entity type, if one exists.
	 * @param type The entity type to get the instancing controller for.
	 * @param <T> The type of the entity.
	 * @return The instancing controller for the given entity type, or {@code null} if none exists.
	 */
	@Nullable
	public static <T extends Entity> EntityInstancingController<? super T> getController(EntityType<T> type) {
		return InstancingControllerRegistryImpl.getController(type);
	}

	/**
	 * Sets the instancing controller for the given block entity type.
	 * @param type The block entity type to set the instancing controller for.
	 * @param instancingController The instancing controller to set.
	 * @param <T> The type of the block entity.
	 */
	public static <T extends BlockEntity> void setController(BlockEntityType<T> type, BlockEntityInstancingController<? super T> instancingController) {
		InstancingControllerRegistryImpl.setController(type, instancingController);
	}

	/**
	 * Sets the instancing controller for the given entity type.
	 * @param type The entity type to set the instancing controller for.
	 * @param instancingController The instancing controller to set.
	 * @param <T> The type of the entity.
	 */
	public static <T extends Entity> void setController(EntityType<T> type, EntityInstancingController<? super T> instancingController) {
		InstancingControllerRegistryImpl.setController(type, instancingController);
	}

	private InstancingControllerRegistry() {
	}
}
