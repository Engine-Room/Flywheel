package com.jozufozu.flywheel.lib.instance;

import java.util.Objects;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.api.instance.BlockEntityInstance;
import com.jozufozu.flywheel.api.instance.controller.BlockEntityInstancingController;
import com.jozufozu.flywheel.api.instance.controller.InstanceContext;
import com.jozufozu.flywheel.api.instance.controller.InstancingControllerRegistry;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class SimpleBlockEntityInstancingController<T extends BlockEntity> implements BlockEntityInstancingController<T> {
	protected Factory<T> instanceFactory;
	protected Predicate<T> skipRender;

	public SimpleBlockEntityInstancingController(Factory<T> instanceFactory, Predicate<T> skipRender) {
		this.instanceFactory = instanceFactory;
		this.skipRender = skipRender;
	}

	@Override
	public BlockEntityInstance<? super T> createInstance(InstanceContext ctx, T blockEntity) {
		return instanceFactory.create(ctx, blockEntity);
	}

	@Override
	public boolean shouldSkipRender(T blockEntity) {
		return skipRender.test(blockEntity);
	}

	/**
	 * Get an object to configure the instancing controller for the given block entity type.
	 *
	 * @param type The block entity type to configure.
	 * @param <T>  The type of the block entity.
	 * @return The configuration object.
	 */
	public static <T extends BlockEntity> BlockEntityConfig<T> configure(BlockEntityType<T> type) {
		return new BlockEntityConfig<>(type);
	}

	@FunctionalInterface
	public interface Factory<T extends BlockEntity> {
		@NotNull BlockEntityInstance<? super T> create(InstanceContext ctx, T blockEntity);
	}

	/**
	 * An object to configure the instancing controller for a block entity.
	 *
	 * @param <T> The type of the block entity.
	 */
	public static class BlockEntityConfig<T extends BlockEntity> {
		protected BlockEntityType<T> type;
		protected Factory<T> instanceFactory;
		protected Predicate<T> skipRender;

		public BlockEntityConfig(BlockEntityType<T> type) {
			this.type = type;
		}

		/**
		 * Sets the instance factory for the block entity.
		 *
		 * @param instanceFactory The instance factory.
		 * @return {@code this}
		 */
		public BlockEntityConfig<T> factory(Factory<T> instanceFactory) {
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
			InstancingControllerRegistry.setController(type, controller);
			return controller;
		}
	}
}
