package com.jozufozu.flywheel.api.instance;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.instance.blockentity.BlockEntityInstancingController;
import com.jozufozu.flywheel.api.instance.entity.EntityInstancingController;
import com.jozufozu.flywheel.api.instancer.InstancerProvider;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.backend.instancing.entity.EntityInstance;
import com.jozufozu.flywheel.extension.BlockEntityTypeExtension;
import com.jozufozu.flywheel.extension.EntityTypeExtension;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * A utility class for registering and retrieving {@code InstancingController}s.
 */
@SuppressWarnings("unchecked")
public class InstancedRenderRegistry {
	/**
	 * Checks if the given block entity type can be instanced.
	 * @param type The block entity type to check.
	 * @param <T> The type of the block entity.
	 * @return {@code true} if the block entity type can be instanced.
	 */
	public static <T extends BlockEntity> boolean canInstance(BlockEntityType<? extends T> type) {
		return getController(type) != null;
	}

	/**
	 * Checks if the given entity type can be instanced.
	 * @param type The entity type to check.
	 * @param <T> The type of the entity.
	 * @return {@code true} if the entity type can be instanced.
	 */
	public static <T extends Entity> boolean canInstance(EntityType<? extends T> type) {
		return getController(type) != null;
	}

	/**
	 * Creates an instance for the given block entity, if possible.
	 * @param instancerManager The instancer manager to use.
	 * @param blockEntity The block entity to create an instance of.
	 * @param <T> The type of the block entity.
	 * @return An instance of the block entity, or {@code null} if the block entity cannot be instanced.
	 */
	@Nullable
	public static <T extends BlockEntity> BlockEntityInstance<? super T> createInstance(InstancerProvider instancerManager, T blockEntity) {
		BlockEntityInstancingController<? super T> controller = getController(getType(blockEntity));
		if (controller == null) {
			return null;
		}
		return controller.createInstance(instancerManager, blockEntity);
	}

	/**
	 * Creates an instance for the given entity, if possible.
	 * @param instancerManager The instancer manager to use.
	 * @param entity The entity to create an instance of.
	 * @param <T> The type of the entity.
	 * @return An instance of the entity, or {@code null} if the entity cannot be instanced.
	 */
	@Nullable
	public static <T extends Entity> EntityInstance<? super T> createInstance(InstancerProvider instancerManager, T entity) {
		EntityInstancingController<? super T> controller = getController(getType(entity));
		if (controller == null) {
			return null;
		}
		return controller.createInstance(instancerManager, entity);
	}

	/**
	 * Checks if the given block entity is instanced and should not be rendered normally.
	 * @param blockEntity The block entity to check.
	 * @param <T> The type of the block entity.
	 * @return {@code true} if the block entity is instanced and should not be rendered normally.
	 */
	public static <T extends BlockEntity> boolean shouldSkipRender(T blockEntity) {
		BlockEntityInstancingController<? super T> controller = getController(getType(blockEntity));
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
		EntityInstancingController<? super T> controller = getController(getType(entity));
		if (controller == null) {
			return false;
		}
		return controller.shouldSkipRender(entity);
	}

	/**
	 * Gets the instancing controller for the given block entity type, if one exists.
	 * @param type The block entity type to get the instancing controller for.
	 * @param <T> The type of the block entity.
	 * @return The instancing controller for the given block entity type, or {@code null} if none exists.
	 */
	@Nullable
	public static <T extends BlockEntity> BlockEntityInstancingController<? super T> getController(BlockEntityType<T> type) {
		return ((BlockEntityTypeExtension<T>) type).flywheel$getInstancingController();
	}

	/**
	 * Gets the instancing controller for the given entity type, if one exists.
	 * @param type The entity type to get the instancing controller for.
	 * @param <T> The type of the entity.
	 * @return The instancing controller for the given entity type, or {@code null} if none exists.
	 */
	@Nullable
	public static <T extends Entity> EntityInstancingController<? super T> getController(EntityType<T> type) {
		return ((EntityTypeExtension<T>) type).flywheel$getInstancingController();
	}

	/**
	 * Sets the instancing controller for the given block entity type.
	 * @param type The block entity type to set the instancing controller for.
	 * @param instancingController The instancing controller to set.
	 * @param <T> The type of the block entity.
	 */
	public static <T extends BlockEntity> void setController(BlockEntityType<T> type, BlockEntityInstancingController<? super T> instancingController) {
		((BlockEntityTypeExtension<T>) type).flywheel$setInstancingController(instancingController);
	}

	/**
	 * Sets the instancing controller for the given entity type.
	 * @param type The entity type to set the instancing controller for.
	 * @param instancingController The instancing controller to set.
	 * @param <T> The type of the entity.
	 */
	public static <T extends Entity> void setController(EntityType<T> type, EntityInstancingController<? super T> instancingController) {
		((EntityTypeExtension<T>) type).flywheel$setInstancingController(instancingController);
	}

	/**
	 * Gets the type of the given block entity.
	 * @param blockEntity The block entity to get the type of.
	 * @param <T> The type of the block entity.
	 * @return The {@link BlockEntityType} associated with the given block entity.
	 */
	public static <T extends BlockEntity> BlockEntityType<? super T> getType(T blockEntity) {
		return (BlockEntityType<? super T>) blockEntity.getType();
	}

	/**
	 * Gets the type of the given entity.
	 * @param entity The entity to get the type of.
	 * @param <T> The type of the entity.
	 * @return The {@link EntityType} associated with the given entity.
	 */
	public static <T extends Entity> EntityType<? super T> getType(T entity) {
		return (EntityType<? super T>) entity.getType();
	}
}
