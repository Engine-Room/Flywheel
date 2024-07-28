package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class OriginBlockAndTintGetter extends VirtualBlockGetter {
	@Nullable
	protected BlockEntity originBlockEntity;
	protected BlockState originBlockState = Blocks.AIR.defaultBlockState();

	public OriginBlockAndTintGetter(ToIntFunction<BlockPos> blockLightFunc, ToIntFunction<BlockPos> skyLightFunc) {
		super(blockLightFunc, skyLightFunc);
	}

	public void originBlockEntity(@Nullable BlockEntity blockEntity) {
		originBlockEntity = blockEntity;
	}

	public void originBlockState(BlockState state) {
		originBlockState = state;
	}

	@Override
	@Nullable
	public BlockEntity getBlockEntity(BlockPos pos) {
		if (pos.equals(BlockPos.ZERO)) {
			return originBlockEntity;
		}

		return null;
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		if (pos.equals(BlockPos.ZERO)) {
			return originBlockState;
		}

		return Blocks.AIR.defaultBlockState();
	}

	@Override
	public int getHeight() {
		return 1;
	}

	@Override
	public int getMinBuildHeight() {
		return 0;
	}
}
