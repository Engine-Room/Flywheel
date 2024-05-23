package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class VirtualEmptyBlockGetter extends VirtualBlockGetter {
	public static final VirtualEmptyBlockGetter INSTANCE = new VirtualEmptyBlockGetter(p -> 0, p -> 15);
	public static final VirtualEmptyBlockGetter FULL_BRIGHT = new VirtualEmptyBlockGetter(p -> 15, p -> 15);
	public static final VirtualEmptyBlockGetter FULL_DARK = new VirtualEmptyBlockGetter(p -> 0, p -> 0);

	public VirtualEmptyBlockGetter(ToIntFunction<BlockPos> blockLightFunc, ToIntFunction<BlockPos> skyLightFunc) {
		super(blockLightFunc, skyLightFunc);
	}

	public static boolean is(BlockAndTintGetter blockGetter) {
		return blockGetter instanceof VirtualEmptyBlockGetter;
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
