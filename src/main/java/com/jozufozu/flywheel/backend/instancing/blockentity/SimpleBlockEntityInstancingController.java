package com.jozufozu.flywheel.backend.instancing.blockentity;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.jozufozu.flywheel.api.instancer.InstancerManager;

import net.minecraft.world.level.block.entity.BlockEntity;

public class SimpleBlockEntityInstancingController<T extends BlockEntity> implements BlockEntityInstancingController<T> {
	protected BiFunction<InstancerManager, T, BlockEntityInstance<? super T>> instanceFactory;
	protected Predicate<T> skipRender;

	public SimpleBlockEntityInstancingController(BiFunction<InstancerManager, T, BlockEntityInstance<? super T>> instanceFactory, Predicate<T> skipRender) {
		this.instanceFactory = instanceFactory;
		this.skipRender = skipRender;
	}

	@Override
	public BlockEntityInstance<? super T> createInstance(InstancerManager instancerManager, T blockEntity) {
		return instanceFactory.apply(instancerManager, blockEntity);
	}

	@Override
	public boolean shouldSkipRender(T blockEntity) {
		return skipRender.test(blockEntity);
	}
}
