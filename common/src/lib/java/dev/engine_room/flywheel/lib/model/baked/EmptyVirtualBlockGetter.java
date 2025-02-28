package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class EmptyVirtualBlockGetter extends VirtualBlockGetter {
	public static final EmptyVirtualBlockGetter FULL_DARK = new EmptyVirtualBlockGetter(p -> 0, p -> 0);
	public static final EmptyVirtualBlockGetter FULL_BRIGHT = new EmptyVirtualBlockGetter(p -> 15, p -> 15);

	public EmptyVirtualBlockGetter(ToIntFunction<BlockPos> blockLightFunc, ToIntFunction<BlockPos> skyLightFunc) {
		super(blockLightFunc, skyLightFunc);
	}

	@Override
	@Nullable
	public final BlockEntity getBlockEntity(BlockPos pos) {
		return null;
	}

	@Override
	public final BlockState getBlockState(BlockPos pos) {
		return Blocks.AIR.defaultBlockState();
	}

	@Override
	public final FluidState getFluidState(BlockPos pos) {
		return Fluids.EMPTY.defaultFluidState();
	}

	@Override
	public final int getHeight() {
		return 1;
	}

	@Override
	public final int getMinBuildHeight() {
		return 0;
	}
}
