package dev.engine_room.flywheel.backend;

import org.jetbrains.annotations.UnknownNullability;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public class FlwBackendXplatImpl implements FlwBackendXplat {
	@UnknownNullability
	public static BackendConfig CONFIG;

	@Override
	public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getLightEmission(level, pos);
	}

	@Override
	public BackendConfig getConfig() {
		return CONFIG;
	}
}
