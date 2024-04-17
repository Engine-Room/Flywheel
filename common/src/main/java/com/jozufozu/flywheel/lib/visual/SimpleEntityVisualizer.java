package com.jozufozu.flywheel.lib.visual;

import java.util.Objects;
import java.util.function.Predicate;

import com.jozufozu.flywheel.api.visual.EntityVisual;
import com.jozufozu.flywheel.api.visualization.EntityVisualizer;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.api.visualization.VisualizerRegistry;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class SimpleEntityVisualizer<T extends Entity> implements EntityVisualizer<T> {
	protected Factory<T> visualFactory;
	protected Predicate<T> skipVanillaRender;

	public SimpleEntityVisualizer(Factory<T> visualFactory, Predicate<T> skipVanillaRender) {
		this.visualFactory = visualFactory;
		this.skipVanillaRender = skipVanillaRender;
	}

	@Override
	public EntityVisual<? super T> createVisual(VisualizationContext ctx, T entity) {
		return visualFactory.create(ctx, entity);
	}

	@Override
	public boolean skipVanillaRender(T entity) {
		return skipVanillaRender.test(entity);
	}

	/**
	 * Get an object to configure the visualizer for the given entity type.
	 *
	 * @param type The entity type to configure.
	 * @param <T>  The type of the entity.
	 * @return The configuration object.
	 */
	public static <T extends Entity> Builder<T> builder(EntityType<T> type) {
		return new Builder<>(type);
	}

	@FunctionalInterface
	public interface Factory<T extends Entity> {
		EntityVisual<? super T> create(VisualizationContext ctx, T entity);
	}

	/**
	 * An object to configure the visualizer for an entity.
	 *
	 * @param <T> The type of the entity.
	 */
	public static class Builder<T extends Entity> {
		protected EntityType<T> type;
		protected Factory<T> visualFactory;
		protected Predicate<T> skipVanillaRender;

		public Builder(EntityType<T> type) {
			this.type = type;
		}

		/**
		 * Sets the visual factory for the entity.
		 *
		 * @param visualFactory The visual factory.
		 * @return {@code this}
		 */
		public Builder<T> factory(Factory<T> visualFactory) {
			this.visualFactory = visualFactory;
			return this;
		}

		/**
		 * Sets a predicate to determine whether to skip rendering with the vanilla {@link EntityRenderer}.
		 *
		 * @param skipVanillaRender The predicate.
		 * @return {@code this}
		 */
		public Builder<T> skipVanillaRender(Predicate<T> skipVanillaRender) {
			this.skipVanillaRender = skipVanillaRender;
			return this;
		}

		/**
		 * Sets a predicate to always skip rendering with the vanilla {@link EntityRenderer}.
		 *
		 * @return {@code this}
		 */
		public Builder<T> neverSkipVanillaRender() {
			this.skipVanillaRender = entity -> false;
			return this;
		}

		/**
		 * Constructs the entity visualizer and sets it for the entity type.
		 *
		 * @return The entity visualizer.
		 */
		public SimpleEntityVisualizer<T> apply() {
			Objects.requireNonNull(visualFactory, "Visual factory cannot be null!");
			if (skipVanillaRender == null) {
				skipVanillaRender = entity -> true;
			}

			SimpleEntityVisualizer<T> visualizer = new SimpleEntityVisualizer<>(visualFactory, skipVanillaRender);
			VisualizerRegistry.setVisualizer(type, visualizer);
			return visualizer;
		}
	}
}
