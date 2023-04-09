package com.jozufozu.flywheel.lib.visual;

import java.util.Objects;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.api.visual.BlockEntityVisual;
import com.jozufozu.flywheel.api.visualization.BlockEntityVisualizer;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.api.visualization.VisualizerRegistry;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class SimpleBlockEntityVisualizer<T extends BlockEntity> implements BlockEntityVisualizer<T> {
	protected Factory<T> visualFactory;
	protected Predicate<T> skipRender;

	public SimpleBlockEntityVisualizer(Factory<T> visualFactory, Predicate<T> skipRender) {
		this.visualFactory = visualFactory;
		this.skipRender = skipRender;
	}

	@Override
	public BlockEntityVisual<? super T> createVisual(VisualizationContext ctx, T blockEntity) {
		return visualFactory.create(ctx, blockEntity);
	}

	@Override
	public boolean shouldSkipRender(T blockEntity) {
		return skipRender.test(blockEntity);
	}

	/**
	 * Get an object to configure the visualizer for the given block entity type.
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
		@NotNull BlockEntityVisual<? super T> create(VisualizationContext ctx, T blockEntity);
	}

	/**
	 * An object to configure the visualizer for a block entity.
	 *
	 * @param <T> The type of the block entity.
	 */
	public static class BlockEntityConfig<T extends BlockEntity> {
		protected BlockEntityType<T> type;
		protected Factory<T> visualFactory;
		protected Predicate<T> skipRender;

		public BlockEntityConfig(BlockEntityType<T> type) {
			this.type = type;
		}

		/**
		 * Sets the visual factory for the block entity.
		 *
		 * @param visualFactory The visual factory.
		 * @return {@code this}
		 */
		public BlockEntityConfig<T> factory(Factory<T> visualFactory) {
			this.visualFactory = visualFactory;
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
		 * Constructs the block entity visualizer, and sets it for the block entity type.
		 * @return The block entity visualizer.
		 */
		public SimpleBlockEntityVisualizer<T> apply() {
			Objects.requireNonNull(visualFactory, "Visual factory cannot be null!");
			if (skipRender == null) {
				skipRender = be -> false;
			}
			SimpleBlockEntityVisualizer<T> visualizer = new SimpleBlockEntityVisualizer<>(visualFactory, skipRender);
			VisualizerRegistry.setVisualizer(type, visualizer);
			return visualizer;
		}
	}
}
