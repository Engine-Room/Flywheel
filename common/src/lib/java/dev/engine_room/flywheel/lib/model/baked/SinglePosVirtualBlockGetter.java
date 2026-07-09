package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SinglePosVirtualBlockGetter extends VirtualBlockGetter {
	protected BlockPos pos = BlockPos.ZERO;
	protected BlockState blockState = Blocks.AIR.defaultBlockState();
	@Nullable
	protected BlockEntity blockEntity;

	public SinglePosVirtualBlockGetter(ToIntFunction<BlockPos> blockLightFunc, ToIntFunction<BlockPos> skyLightFunc) {
		super(blockLightFunc, skyLightFunc);
	}

	public static SinglePosVirtualBlockGetter createFullDark() {
		return new SinglePosVirtualBlockGetter(p -> 0, p -> 0);
	}

	public static SinglePosVirtualBlockGetter createFullBright() {
		return new SinglePosVirtualBlockGetter(p -> 15, p -> 15);
	}

	public SinglePosVirtualBlockGetter pos(BlockPos pos) {
		this.pos = pos;
		return this;
	}

	public SinglePosVirtualBlockGetter blockState(BlockState state) {
		blockState = state;
		return this;
	}

	public SinglePosVirtualBlockGetter blockEntity(@Nullable BlockEntity blockEntity) {
		this.blockEntity = blockEntity;
		return this;
	}

	@Override
	@Nullable
	public BlockEntity getBlockEntity(BlockPos pos) {
		if (pos.equals(this.pos)) {
			return blockEntity;
		}

		return null;
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		if (pos.equals(this.pos)) {
			return blockState;
		}

		return Blocks.AIR.defaultBlockState();
	}

	public int getHeight() {
		return 1;
	}

	public int getMinBuildHeight() {
		return pos.getY();
	}

	@Override
	public int getMinY() {
		return pos.getY();
	}
}
