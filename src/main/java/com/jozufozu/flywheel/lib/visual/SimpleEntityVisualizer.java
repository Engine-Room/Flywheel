package com.jozufozu.flywheel.lib.visual;

import java.util.Objects;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.api.visual.EntityVisual;
import com.jozufozu.flywheel.api.visualization.EntityVisualizer;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.api.visualization.VisualizerRegistry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class SimpleEntityVisualizer<T extends Entity> implements EntityVisualizer<T> {
	protected Factory<T> visualFactory;
	protected Predicate<T> skipRender;

	public SimpleEntityVisualizer(Factory<T> visualFactory, Predicate<T> skipRender) {
		this.visualFactory = visualFactory;
		this.skipRender = skipRender;
	}

	@Override
	public EntityVisual<? super T> createVisual(VisualizationContext ctx, T entity) {
		return visualFactory.create(ctx, entity);
	}

	@Override
	public boolean shouldSkipRender(T entity) {
		return skipRender.test(entity);
	}

	/**
	 * Get an object to configure the visualizer for the given entity type.
	 *
	 * @param type The entity type to configure.
	 * @param <T>  The type of the entity.
	 * @return The configuration object.
	 */
	public static <T extends Entity> EntityConfig<T> configure(EntityType<T> type) {
		return new EntityConfig<>(type);
	}

	@FunctionalInterface
	public interface Factory<T extends Entity> {
		@NotNull EntityVisual<? super T> create(VisualizationContext ctx, T entity);
	}

	/**
	 * An object to configure the visualizer for an entity.
	 *
	 * @param <T> The type of the entity.
	 */
	public static class EntityConfig<T extends Entity> {
		protected EntityType<T> type;
		protected Factory<T> visualFactory;
		protected Predicate<T> skipRender;

		public EntityConfig(EntityType<T> type) {
			this.type = type;
		}

		/**
		 * Sets the visual factory for the entity.
		 *
		 * @param visualFactory The visual factory.
		 * @return {@code this}
		 */
		public EntityConfig<T> factory(Factory<T> visualFactory) {
			this.visualFactory = visualFactory;
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
		 * Constructs the entity visualizer, and sets it for the entity type.
		 * @return The entity visualizer.
		 */
		public SimpleEntityVisualizer<T> apply() {
			Objects.requireNonNull(visualFactory, "Visual factory cannot be null!");
			if (skipRender == null) {
				skipRender = entity -> false;
			}
			SimpleEntityVisualizer<T> visualizer = new SimpleEntityVisualizer<>(visualFactory, skipRender);
			VisualizerRegistry.setVisualizer(type, visualizer);
			return visualizer;
		}
	}
}
