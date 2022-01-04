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

public class InstancedRenderRegistry {
	public static <T extends BlockEntity> boolean canInstance(BlockEntityType<? extends T> type) {
		return getBlockEntityController(type) != null;
	}

	public static <T extends Entity> boolean canInstance(EntityType<? extends T> type) {
		return getEntityController(type) != null;
	}

	@Nullable
	public static <T extends BlockEntity> BlockEntityInstance<? super T> createInstance(MaterialManager materialManager, T blockEntity) {
		BlockEntityInstancingController<? super T> controller = getBlockEntityController(getType(blockEntity));
		if (controller == null) {
			return null;
		}
		return controller.createInstance(materialManager, blockEntity);
	}

	@Nullable
	public static <T extends Entity> EntityInstance<? super T> createInstance(MaterialManager materialManager, T entity) {
		EntityInstancingController<? super T> controller = getEntityController(getType(entity));
		if (controller == null) {
			return null;
		}
		return controller.createInstance(materialManager, entity);
	}

	public static <T extends BlockEntity> boolean shouldSkipRender(T blockEntity) {
		BlockEntityInstancingController<? super T> controller = getBlockEntityController(getType(blockEntity));
		if (controller == null) {
			return false;
		}
		return controller.shouldSkipRender(blockEntity);
	}

	public static <T extends Entity> boolean shouldSkipRender(T entity) {
		EntityInstancingController<? super T> controller = getEntityController(getType(entity));
		if (controller == null) {
			return false;
		}
		return controller.shouldSkipRender(entity);
	}

	public static <T extends BlockEntity> BlockEntityConfig<T> configure(BlockEntityType<T> type) {
		return new BlockEntityConfig<>(type);
	}

	public static <T extends Entity> EntityConfig<T> configure(EntityType<T> type) {
		return new EntityConfig<>(type);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public static <T extends BlockEntity> BlockEntityInstancingController<? super T> getBlockEntityController(BlockEntityType<T> type) {
		return ((BlockEntityTypeExtension<T>) type).flywheel$getInstancingController();
	}

	@SuppressWarnings("unchecked")
	public static <T extends BlockEntity> void setBlockEntityController(BlockEntityType<T> type, BlockEntityInstancingController<? super T> instancingController) {
		((BlockEntityTypeExtension<T>) type).flywheel$setInstancingController(instancingController);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public static <T extends Entity> EntityInstancingController<? super T> getEntityController(EntityType<T> type) {
		return ((EntityTypeExtension<T>) type).flywheel$getInstancingController();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Entity> void setEntityController(EntityType<T> type, EntityInstancingController<? super T> instancingController) {
		((EntityTypeExtension<T>) type).flywheel$setInstancingController(instancingController);
	}

	@SuppressWarnings("unchecked")
	public static <T extends BlockEntity> BlockEntityType<? super T> getType(T blockEntity) {
		return (BlockEntityType<? super T>) blockEntity.getType();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Entity> EntityType<? super T> getType(T entity) {
		return (EntityType<? super T>) entity.getType();
	}

	public static class BlockEntityConfig<T extends BlockEntity> {
		protected BlockEntityType<T> type;
		protected BiFunction<MaterialManager, T, BlockEntityInstance<? super T>> instanceFactory;
		protected Predicate<T> skipRender;

		public BlockEntityConfig(BlockEntityType<T> type) {
			this.type = type;
		}

		public BlockEntityConfig<T> factory(BiFunction<MaterialManager, T, BlockEntityInstance<? super T>> instanceFactory) {
			this.instanceFactory = instanceFactory;
			return this;
		}

		public BlockEntityConfig<T> skipRender(Predicate<T> skipRender) {
			this.skipRender = skipRender;
			return this;
		}

		public BlockEntityConfig<T> alwaysSkipRender() {
			this.skipRender = be -> true;
			return this;
		}

		public SimpleBlockEntityInstancingController<T> apply() {
			Objects.requireNonNull(instanceFactory, "Instance factory cannot be null!");
			if (skipRender == null) {
				skipRender = be -> false;
			}
			SimpleBlockEntityInstancingController<T> controller = new SimpleBlockEntityInstancingController<>(instanceFactory, skipRender);
			setBlockEntityController(type, controller);
			return controller;
		}
	}

	public static class EntityConfig<T extends Entity> {
		protected EntityType<T> type;
		protected BiFunction<MaterialManager, T, EntityInstance<? super T>> instanceFactory;
		protected Predicate<T> skipRender;

		public EntityConfig(EntityType<T> type) {
			this.type = type;
		}

		public EntityConfig<T> factory(BiFunction<MaterialManager, T, EntityInstance<? super T>> instanceFactory) {
			this.instanceFactory = instanceFactory;
			return this;
		}

		public EntityConfig<T> skipRender(Predicate<T> skipRender) {
			this.skipRender = skipRender;
			return this;
		}

		public EntityConfig<T> alwaysSkipRender() {
			this.skipRender = entity -> true;
			return this;
		}

		public SimpleEntityInstancingController<T> apply() {
			Objects.requireNonNull(instanceFactory, "Instance factory cannot be null!");
			if (skipRender == null) {
				skipRender = entity -> false;
			}
			SimpleEntityInstancingController<T> controller = new SimpleEntityInstancingController<>(instanceFactory, skipRender);
			setEntityController(type, controller);
			return controller;
		}
	}
}
