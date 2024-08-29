package com.jozufozu.flywheel.compat;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;

import net.caffeinemc.mods.sodium.api.blockentity.BlockEntityRenderHandler;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class SodiumCompat {
	public static void forBlockEntityType(BlockEntityType<?> type) {
		BlockEntityRenderHandler.instance().addRenderPredicate(type, (blockGetter, blockPos, blockEntity) ->
			InstancedRenderDispatcher.tryAddBlockEntity(blockEntity)
		);
	}
}
