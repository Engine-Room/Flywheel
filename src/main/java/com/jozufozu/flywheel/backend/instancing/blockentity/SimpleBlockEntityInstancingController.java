package com.jozufozu.flywheel.backend.instancing.blockentity;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.jozufozu.flywheel.api.MaterialManager;

import net.minecraft.world.level.block.entity.BlockEntity;

public class SimpleBlockEntityInstancingController<T extends BlockEntity> implements BlockEntityInstancingController<T> {
	protected BiFunction<MaterialManager, T, BlockEntityInstance<? super T>> instanceFactory;
	protected Predicate<T> skipRender;

	public SimpleBlockEntityInstancingController(BiFunction<MaterialManager, T, BlockEntityInstance<? super T>> instanceFactory, Predicate<T> skipRender) {
		this.instanceFactory = instanceFactory;
		this.skipRender = skipRender;
	}

	@Override
	public BlockEntityInstance<? super T> createInstance(MaterialManager materialManager, T blockEntity) {
		return instanceFactory.apply(materialManager, blockEntity);
	}

	@Override
	public boolean shouldSkipRender(T blockEntity) {
		return skipRender.test(blockEntity);
	}
}
