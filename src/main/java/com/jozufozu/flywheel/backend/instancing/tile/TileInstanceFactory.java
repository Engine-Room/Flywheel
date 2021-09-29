package com.jozufozu.flywheel.backend.instancing.tile;

import com.jozufozu.flywheel.backend.material.MaterialManager;

import net.minecraft.world.level.block.entity.BlockEntity;

@FunctionalInterface
public interface TileInstanceFactory<T extends BlockEntity> {
	TileEntityInstance<? super T> create(MaterialManager manager, T te);
}
