package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FabricSinglePosVirtualBlockGetter extends SinglePosVirtualBlockGetter {
	@Nullable
	protected Object renderData;

	public FabricSinglePosVirtualBlockGetter(ToIntFunction<BlockPos> blockLightFunc, ToIntFunction<BlockPos> skyLightFunc) {
		super(blockLightFunc, skyLightFunc);
	}

	public static FabricSinglePosVirtualBlockGetter createFullDark() {
		return new FabricSinglePosVirtualBlockGetter(p -> 0, p -> 0);
	}

	public static FabricSinglePosVirtualBlockGetter createFullBright() {
		return new FabricSinglePosVirtualBlockGetter(p -> 15, p -> 15);
	}

	@Override
	public FabricSinglePosVirtualBlockGetter pos(BlockPos pos) {
		super.pos(pos);
		return this;
	}

	@Override
	public FabricSinglePosVirtualBlockGetter blockState(BlockState state) {
		super.blockState(blockState);
		return this;
	}

	@Override
	public FabricSinglePosVirtualBlockGetter blockEntity(@Nullable BlockEntity blockEntity) {
		super.blockEntity(blockEntity);
		return this;
	}

	public FabricSinglePosVirtualBlockGetter renderData(@Nullable Object renderData) {
		this.renderData = renderData;
		return this;
	}

	@Override
	@Nullable
	public Object getBlockEntityRenderData(BlockPos pos) {
		if (pos.equals(this.pos)) {
			return renderData != null ? renderData : super.getBlockEntityRenderData(pos);
		}

		return super.getBlockEntityRenderData(pos);
	}
}
