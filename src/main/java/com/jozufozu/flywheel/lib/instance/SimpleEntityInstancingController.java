package com.jozufozu.flywheel.lib.instance;

import java.util.Objects;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.api.instance.EntityInstance;
import com.jozufozu.flywheel.api.instance.controller.EntityInstancingController;
import com.jozufozu.flywheel.api.instance.controller.InstanceContext;
import com.jozufozu.flywheel.api.instance.controller.InstancingControllerRegistry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class SimpleEntityInstancingController<T extends Entity> implements EntityInstancingController<T> {
	protected Factory<T> instanceFactory;
	protected Predicate<T> skipRender;

	public SimpleEntityInstancingController(Factory<T> instanceFactory, Predicate<T> skipRender) {
		this.instanceFactory = instanceFactory;
		this.skipRender = skipRender;
	}

	@Override
	public EntityInstance<? super T> createInstance(InstanceContext ctx, T entity) {
		return instanceFactory.create(ctx, entity);
	}

	@Override
	public boolean shouldSkipRender(T entity) {
		return skipRender.test(entity);
	}

	/**
	 * Get an object to configure the instancing controller for the given entity type.
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
		@NotNull EntityInstance<? super T> create(InstanceContext ctx, T entity);
	}

	/**
	 * An object to configure the instancing controller for an entity.
	 *
	 * @param <T> The type of the entity.
	 */
	public static class EntityConfig<T extends Entity> {
		protected EntityType<T> type;
		protected Factory<T> instanceFactory;
		protected Predicate<T> skipRender;

		public EntityConfig(EntityType<T> type) {
			this.type = type;
		}

		/**
		 * Sets the instance factory for the entity.
		 *
		 * @param instanceFactory The instance factory.
		 * @return {@code this}
		 */
		public EntityConfig<T> factory(Factory<T> instanceFactory) {
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
			InstancingControllerRegistry.setController(type, controller);
			return controller;
		}
	}
}
