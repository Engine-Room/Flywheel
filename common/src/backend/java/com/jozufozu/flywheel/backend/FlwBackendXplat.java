package com.jozufozu.flywheel.backend;

import com.jozufozu.flywheel.api.internal.DependencyInjection;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface FlwBackendXplat {
	FlwBackendXplat INSTANCE = DependencyInjection.load(FlwBackendXplat.class, "com.jozufozu.flywheel.backend.FlwBackendXplatImpl");

	int getLightEmission(BlockState state, BlockGetter level, BlockPos pos);
}
