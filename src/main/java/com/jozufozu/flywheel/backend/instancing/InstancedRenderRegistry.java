package com.jozufozu.flywheel.backend.instancing;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstancingController;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityTypeExtension;
import com.jozufozu.flywheel.backend.instancing.blockentity.SimpleBlockEntityInstancingController;
import com.jozufozu.flywheel.backend.instancing.entity.EntityInstance;
import com.jozufozu.flywheel.backend.instancing.entity.EntityInstancingController;
import com.jozufozu.flywheel.backend.instancing.entity.EntityTypeExtension;
import com.jozufozu.flywheel.backend.instancing.entity.SimpleEntityInstancingController;

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
	 * @param materialManager The material manager to use.
	 * @param blockEntity The block entity to create an instance of.
	 * @param <T> The type of the block entity.
	 * @return An instance of the block entity, or {@code null} if the block entity cannot be instanced.
	 */
	@Nullable
	public static <T extends BlockEntity> BlockEntityInstance<? super T> createInstance(MaterialManager materialManager, T blockEntity) {
		BlockEntityInstancingController<? super T> controller = getController(getType(blockEntity));
		if (controller == null) {
			return null;
		}
		return controller.createInstance(materialManager, blockEntity);
	}

	/**
	 * Creates an instance for the given entity, if possible.
	 * @param materialManager The material manager to use.
	 * @param entity The entity to create an instance of.
	 * @param <T> The type of the entity.
	 * @return An instance of the entity, or {@code null} if the entity cannot be instanced.
	 */
	@Nullable
	public static <T extends Entity> EntityInstance<? super T> createInstance(MaterialManager materialManager, T entity) {
		EntityInstancingController<? super T> controller = getController(getType(entity));
		if (controller == null) {
			return null;
		}
		return controller.createInstance(materialManager, entity);
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
	 * Get an object to configure the instancing controller for the given block entity type.
	 * @param type The block entity type to configure.
	 * @param <T> The type of the block entity.
	 * @return The configuration object.
	 */
	public static <T extends BlockEntity> BlockEntityConfig<T> configure(BlockEntityType<T> type) {
		return new BlockEntityConfig<>(type);
	}

	/**
	 * Get an object to configure the instancing controller for the given entity type.
	 * @param type The entity type to configure.
	 * @param <T> The type of the entity.
	 * @return The configuration object.
	 */
	public static <T extends Entity> EntityConfig<T> configure(EntityType<T> type) {
		return new EntityConfig<>(type);
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

	/**
	 * An object to configure the instancing controller for a block entity.
	 * @param <T> The type of the block entity.
	 */
	public static class BlockEntityConfig<T extends BlockEntity> {
		protected BlockEntityType<T> type;
		protected BiFunction<MaterialManager, T, BlockEntityInstance<? super T>> instanceFactory;
		protected Predicate<T> skipRender;

		public BlockEntityConfig(BlockEntityType<T> type) {
			this.type = type;
		}

		/**
		 * Sets the instance factory for the block entity.
		 * @param instanceFactory The instance factory.
		 * @return {@code this}
		 */
		public BlockEntityConfig<T> factory(BiFunction<MaterialManager, T, BlockEntityInstance<? super T>> instanceFactory) {
			this.instanceFactory = instanceFactory;
			return this;
		}

		/**
		 * Sets a predicate to determine whether to skip rendering a block entity.
		 * @param skipRender The predicate.
		 * @return {@code this}
		 */
		public BlockEntityConfig<T> skipRender(Predicate<T> skipRender) {
			this.skipRender = skipRender;
			return this;
		}

		/**
		 * Sets a predicate to always skip rendering for block entities of this type.
		 * @return {@code this}
		 */
		public BlockEntityConfig<T> alwaysSkipRender() {
			this.skipRender = be -> true;
			return this;
		}

		/**
		 * Constructs the block entity instancing controller, and sets it for the block entity type.
		 * @return The block entity instancing controller.
		 */
		public SimpleBlockEntityInstancingController<T> apply() {
			Objects.requireNonNull(instanceFactory, "Instance factory cannot be null!");
			if (skipRender == null) {
				skipRender = be -> false;
			}
			SimpleBlockEntityInstancingController<T> controller = new SimpleBlockEntityInstancingController<>(instanceFactory, skipRender);
			setController(type, controller);
			return controller;
		}
	}

	/**
	 * An object to configure the instancing controller for an entity.
	 * @param <T> The type of the entity.
	 */
	public static class EntityConfig<T extends Entity> {
		protected EntityType<T> type;
		protected BiFunction<MaterialManager, T, EntityInstance<? super T>> instanceFactory;
		protected Predicate<T> skipRender;

		public EntityConfig(EntityType<T> type) {
			this.type = type;
		}

		/**
		 * Sets the instance factory for the entity.
		 * @param instanceFactory The instance factory.
		 * @return {@code this}
		 */
		public EntityConfig<T> factory(BiFunction<MaterialManager, T, EntityInstance<? super T>> instanceFactory) {
			this.instanceFactory = instanceFactory;
			return this;
		}

		/**
		 * Sets a predicate to determine whether to skip rendering an entity.
		 * @param skipRender The predicate.
		 * @return {@code this}
		 */
		public EntityConfig<T> skipRender(Predicate<T> skipRender) {
			this.skipRender = skipRender;
			return this;
		}

		/**
		 * Sets a predicate to always skip rendering for entities of this type.
		 * @return {@code this}
		 */
		public EntityConfig<T> alwaysSkipRender() {
			this.skipRender = entity -> true;
			return this;
		}

		/**
		 * Constructs the entity instancing controller, and sets it for the entity type.
		 * @return The entity instancing controller.
		 */
		public SimpleEntityInstancingController<T> apply() {
			Objects.requireNonNull(instanceFactory, "Instance factory cannot be null!");
			if (skipRender == null) {
				skipRender = entity -> false;
			}
			SimpleEntityInstancingController<T> controller = new SimpleEntityInstancingController<>(instanceFactory, skipRender);
			setController(type, controller);
			return controller;
		}
	}
}
