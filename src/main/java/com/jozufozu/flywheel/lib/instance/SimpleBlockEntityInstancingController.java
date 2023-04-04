package com.jozufozu.flywheel.lib.instance;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.jozufozu.flywheel.api.instance.BlockEntityInstance;
import com.jozufozu.flywheel.api.instance.controller.BlockEntityInstancingController;
import com.jozufozu.flywheel.api.instance.controller.InstancingControllerRegistry;
import com.jozufozu.flywheel.api.instancer.InstancerProvider;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class SimpleBlockEntityInstancingController<T extends BlockEntity> implements BlockEntityInstancingController<T> {
	protected BiFunction<InstancerProvider, T, BlockEntityInstance<? super T>> instanceFactory;
	protected Predicate<T> skipRender;

	public SimpleBlockEntityInstancingController(BiFunction<InstancerProvider, T, BlockEntityInstance<? super T>> instanceFactory, Predicate<T> skipRender) {
		this.instanceFactory = instanceFactory;
		this.skipRender = skipRender;
	}

	@Override
	public BlockEntityInstance<? super T> createInstance(InstancerProvider instancerManager, T blockEntity) {
		return instanceFactory.apply(instancerManager, blockEntity);
	}

	@Override
	public boolean shouldSkipRender(T blockEntity) {
		return skipRender.test(blockEntity);
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
	 * An object to configure the instancing controller for a block entity.
	 * @param <T> The type of the block entity.
	 */
	public static class BlockEntityConfig<T extends BlockEntity> {
		protected BlockEntityType<T> type;
		protected BiFunction<InstancerProvider, T, BlockEntityInstance<? super T>> instanceFactory;
		protected Predicate<T> skipRender;

		public BlockEntityConfig(BlockEntityType<T> type) {
			this.type = type;
		}

		/**
		 * Sets the instance factory for the block entity.
		 * @param instanceFactory The instance factory.
		 * @return {@code this}
		 */
		public BlockEntityConfig<T> factory(BiFunction<InstancerProvider, T, BlockEntityInstance<? super T>> instanceFactory) {
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
