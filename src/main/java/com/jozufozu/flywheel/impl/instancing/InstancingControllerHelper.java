package com.jozufozu.flywheel.impl.instancing;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.instance.controller.BlockEntityInstancingController;
import com.jozufozu.flywheel.api.instance.controller.EntityInstancingController;
import com.jozufozu.flywheel.api.instance.controller.InstancingControllerRegistry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class InstancingControllerHelper {
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T extends BlockEntity> BlockEntityInstancingController<? super T> getController(T blockEntity) {
		return InstancingControllerRegistry.getController((BlockEntityType<? super T>) blockEntity.getType());
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public static <T extends Entity> EntityInstancingController<? super T> getController(T entity) {
		return InstancingControllerRegistry.getController((EntityType<? super T>) entity.getType());
	}

	/**
	 * Checks if the given block entity can be instanced.
	 * @param type The block entity to check.
	 * @param <T> The block entity.
	 * @return {@code true} if the block entity can be instanced.
	 */
	public static <T extends BlockEntity> boolean canInstance(T blockEntity) {
		return getController(blockEntity) != null;
	}

	/**
	 * Checks if the given entity can be instanced.
	 * @param type The entity to check.
	 * @param <T> The entity.
	 * @return {@code true} if the entity can be instanced.
	 */
	public static <T extends Entity> boolean canInstance(T entity) {
		return getController(entity) != null;
	}

	/**
	 * Checks if the given block entity is instanced and should not be rendered normally.
	 * @param blockEntity The block entity to check.
	 * @param <T> The type of the block entity.
	 * @return {@code true} if the block entity is instanced and should not be rendered normally.
	 */
	public static <T extends BlockEntity> boolean shouldSkipRender(T blockEntity) {
		BlockEntityInstancingController<? super T> controller = getController(blockEntity);
		if (controller == null) {
			return false;
		}
		return controller.shouldSkipRender(blockEntity);
	}

	/**
	 * Checks if the given entity is instanced and should not be rendered normally.
	 * @param entity The entity to check.
	 * @param <T> The type of the entity.
	 * @return {@code true} if the entity is instanced and should not be rendered normally.
	 */
	public static <T extends Entity> boolean shouldSkipRender(T entity) {
		EntityInstancingController<? super T> controller = getController(entity);
		if (controller == null) {
			return false;
		}
		return controller.shouldSkipRender(entity);
	}

	private InstancingControllerHelper() {
	}
}
