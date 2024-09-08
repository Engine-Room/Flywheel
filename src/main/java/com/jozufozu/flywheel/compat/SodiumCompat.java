package com.jozufozu.flywheel.compat;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;

import net.caffeinemc.mods.sodium.api.blockentity.BlockEntityRenderHandler;
import net.caffeinemc.mods.sodium.api.blockentity.BlockEntityRenderPredicate;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class SodiumCompat {
	public static <T extends BlockEntity> Object forBlockEntityType(BlockEntityType<T> type) {
		BlockEntityRenderPredicate<T> predicate = (getter, pos, be) -> InstancedRenderDispatcher.tryAddBlockEntity(be);
		BlockEntityRenderHandler.instance().addRenderPredicate(type, predicate);
		return predicate;
	}

	public static <T extends BlockEntity> void removePredicate(BlockEntityType<T> type, Object predicate) {
		BlockEntityRenderHandler.instance().removeRenderPredicate(type, (BlockEntityRenderPredicate<T>) predicate);
	}
}
