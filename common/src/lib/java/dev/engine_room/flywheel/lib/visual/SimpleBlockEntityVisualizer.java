package dev.engine_room.flywheel.lib.visual;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.visual.BlockEntityVisual;
import dev.engine_room.flywheel.api.visualization.BlockEntityVisualizer;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.api.visualization.VisualizerRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class SimpleBlockEntityVisualizer<T extends BlockEntity> implements BlockEntityVisualizer<T> {
	protected Factory<T> visualFactory;
	protected Predicate<T> skipVanillaRender;

	public SimpleBlockEntityVisualizer(Factory<T> visualFactory, Predicate<T> skipVanillaRender) {
		this.visualFactory = visualFactory;
		this.skipVanillaRender = skipVanillaRender;
	}

	@Override
	public List<BlockEntityVisual<? super T>> createVisual(VisualizationContext ctx, T blockEntity) {
		return List.of(visualFactory.create(ctx, blockEntity));
	}

	@Override
	public boolean skipVanillaRender(T blockEntity) {
		return skipVanillaRender.test(blockEntity);
	}

	/**
	 * Get an object to configure the visualizer for the given block entity type.
	 *
	 * @param type The block entity type to configure.
	 * @param <T>  The type of the block entity.
	 * @return The configuration object.
	 */
	public static <T extends BlockEntity> Builder<T> builder(BlockEntityType<T> type) {
		return new Builder<>(type);
	}

	@FunctionalInterface
	public interface Factory<T extends BlockEntity> {
		BlockEntityVisual<? super T> create(VisualizationContext ctx, T blockEntity);
	}

	/**
	 * An object to configure the visualizer for a block entity.
	 *
	 * @param <T> The type of the block entity.
	 */
	public static class Builder<T extends BlockEntity> {
		protected BlockEntityType<T> type;
		@Nullable
		protected Factory<T> visualFactory;
		@Nullable
		protected Predicate<T> skipVanillaRender;

		public Builder(BlockEntityType<T> type) {
			this.type = type;
		}

		/**
		 * Sets the visual factory for the block entity.
		 *
		 * @param visualFactory The visual factory.
		 * @return {@code this}
		 */
		public Builder<T> factory(Factory<T> visualFactory) {
			this.visualFactory = visualFactory;
			return this;
		}

		/**
		 * Sets a predicate to determine whether to skip rendering with the vanilla {@link BlockEntityRenderer}.
		 *
		 * @param skipVanillaRender The predicate.
		 * @return {@code this}
		 */
		public Builder<T> skipVanillaRender(Predicate<T> skipVanillaRender) {
			this.skipVanillaRender = skipVanillaRender;
			return this;
		}

		/**
		 * Sets a predicate to never skip rendering with the vanilla {@link BlockEntityRenderer}.
		 *
		 * @return {@code this}
		 */
		public Builder<T> neverSkipVanillaRender() {
			this.skipVanillaRender = blockEntity -> false;
			return this;
		}

		/**
		 * Constructs the block entity visualizer and sets it for the block entity type.
		 *
		 * @return The block entity visualizer.
		 */
		public SimpleBlockEntityVisualizer<T> apply() {
			Objects.requireNonNull(visualFactory, "Visual factory cannot be null!");
			if (skipVanillaRender == null) {
				skipVanillaRender = blockEntity -> true;
			}

			SimpleBlockEntityVisualizer<T> visualizer = new SimpleBlockEntityVisualizer<>(visualFactory, skipVanillaRender);
			VisualizerRegistry.setVisualizer(type, visualizer);
			return visualizer;
		}
	}
}
