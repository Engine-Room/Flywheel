package dev.engine_room.vanillin.config;

import java.util.Objects;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntityVisualizerBuilder<T extends BlockEntity> {
	private final Configurator configurator;
	private final BlockEntityType<T> type;
	@Nullable
	private SimpleBlockEntityVisualizer.Factory<T> visualFactory;
	@Nullable
	private Predicate<T> skipVanillaRender;

	public BlockEntityVisualizerBuilder(Configurator configurator, BlockEntityType<T> type) {
		this.configurator = configurator;
		this.type = type;
	}

	/**
	 * Sets the visual factory for the block entity.
	 *
	 * @param visualFactory The visual factory.
	 * @return {@code this}
	 */
	public BlockEntityVisualizerBuilder<T> factory(SimpleBlockEntityVisualizer.Factory<T> visualFactory) {
		this.visualFactory = visualFactory;
		return this;
	}

	/**
	 * Sets a predicate to determine whether to skip rendering with the vanilla {@link BlockEntityRenderer}.
	 *
	 * @param skipVanillaRender The predicate.
	 * @return {@code this}
	 */
	public BlockEntityVisualizerBuilder<T> skipVanillaRender(Predicate<T> skipVanillaRender) {
		this.skipVanillaRender = skipVanillaRender;
		return this;
	}

	/**
	 * Sets a predicate to never skip rendering with the vanilla {@link BlockEntityRenderer}.
	 *
	 * @return {@code this}
	 */
	public BlockEntityVisualizerBuilder<T> neverSkipVanillaRender() {
		this.skipVanillaRender = blockEntity -> false;
		return this;
	}

	/**
	 * Constructs the block entity visualizer and sets it for the block entity type.
	 *
	 * @return The block entity visualizer.
	 */
	public SimpleBlockEntityVisualizer<T> apply(boolean enabledByDefault) {
		Objects.requireNonNull(visualFactory, "Visual factory cannot be null!");
		if (skipVanillaRender == null) {
			skipVanillaRender = blockEntity -> true;
		}

		SimpleBlockEntityVisualizer<T> visualizer = new SimpleBlockEntityVisualizer<>(visualFactory, skipVanillaRender);
		configurator.register(type, visualizer, enabledByDefault);

		return visualizer;
	}
}
