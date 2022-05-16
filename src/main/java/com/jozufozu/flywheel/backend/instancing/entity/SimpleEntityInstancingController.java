package com.jozufozu.flywheel.backend.instancing.entity;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.jozufozu.flywheel.api.InstancerManager;

import net.minecraft.world.entity.Entity;

public class SimpleEntityInstancingController<T extends Entity> implements EntityInstancingController<T> {
	protected BiFunction<InstancerManager, T, EntityInstance<? super T>> instanceFactory;
	protected Predicate<T> skipRender;

	public SimpleEntityInstancingController(BiFunction<InstancerManager, T, EntityInstance<? super T>> instanceFactory, Predicate<T> skipRender) {
		this.instanceFactory = instanceFactory;
		this.skipRender = skipRender;
	}

	@Override
	public EntityInstance<? super T> createInstance(InstancerManager instancerManager, T entity) {
		return instanceFactory.apply(instancerManager, entity);
	}

	@Override
	public boolean shouldSkipRender(T entity) {
		return skipRender.test(entity);
	}
}
