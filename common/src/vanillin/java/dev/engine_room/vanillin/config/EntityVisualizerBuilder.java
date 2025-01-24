package dev.engine_room.vanillin.config;

import java.util.Objects;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.lib.visualization.SimpleEntityVisualizer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/**
 * An object to configure the visualizer for an entity.
 *
 * @param <T> The type of the entity.
 */
public final class EntityVisualizerBuilder<T extends Entity> {
	private final Configurator configurator;
	private final EntityType<T> type;
	@Nullable
	private SimpleEntityVisualizer.Factory<T> visualFactory;
	@Nullable
	private Predicate<T> skipVanillaRender;

	public EntityVisualizerBuilder(Configurator configurator, EntityType<T> type) {
		this.configurator = configurator;
		this.type = type;
	}

	/**
	 * Sets the visual factory for the entity.
	 *
	 * @param visualFactory The visual factory.
	 * @return {@code this}
	 */
	public EntityVisualizerBuilder<T> factory(SimpleEntityVisualizer.Factory<T> visualFactory) {
		this.visualFactory = visualFactory;
		return this;
	}

	/**
	 * Sets a predicate to determine whether to skip rendering with the vanilla {@link EntityRenderer}.
	 *
	 * @param skipVanillaRender The predicate.
	 * @return {@code this}
	 */
	public EntityVisualizerBuilder<T> skipVanillaRender(Predicate<T> skipVanillaRender) {
		this.skipVanillaRender = skipVanillaRender;
		return this;
	}

	/**
	 * Sets a predicate to always skip rendering with the vanilla {@link EntityRenderer}.
	 *
	 * @return {@code this}
	 */
	public EntityVisualizerBuilder<T> neverSkipVanillaRender() {
		this.skipVanillaRender = entity -> false;
		return this;
	}

	/**
	 * Constructs the entity visualizer and sets it for the entity type.
	 *
	 * @return The entity visualizer.
	 */
	public SimpleEntityVisualizer<T> apply(boolean enabledByDefault) {
		Objects.requireNonNull(visualFactory, "Visual factory cannot be null!");
		if (skipVanillaRender == null) {
			skipVanillaRender = entity -> true;
		}

		SimpleEntityVisualizer<T> visualizer = new SimpleEntityVisualizer<>(visualFactory, skipVanillaRender);
		configurator.register(type, visualizer, enabledByDefault);

		return visualizer;
	}
}
