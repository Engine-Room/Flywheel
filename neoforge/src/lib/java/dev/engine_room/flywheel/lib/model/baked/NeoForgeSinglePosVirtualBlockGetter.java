package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.ToIntFunction;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public class NeoForgeSinglePosVirtualBlockGetter extends SinglePosVirtualBlockGetter {
	@Nullable
	protected ModelData modelData;

	public NeoForgeSinglePosVirtualBlockGetter(ToIntFunction<BlockPos> blockLightFunc, ToIntFunction<BlockPos> skyLightFunc) {
		super(blockLightFunc, skyLightFunc);
	}

	public static NeoForgeSinglePosVirtualBlockGetter createFullDark() {
		return new NeoForgeSinglePosVirtualBlockGetter(p -> 0, p -> 0);
	}

	public static NeoForgeSinglePosVirtualBlockGetter createFullBright() {
		return new NeoForgeSinglePosVirtualBlockGetter(p -> 15, p -> 15);
	}

	@Override
	public NeoForgeSinglePosVirtualBlockGetter pos(BlockPos pos) {
		super.pos(pos);
		return this;
	}

	@Override
	public NeoForgeSinglePosVirtualBlockGetter blockState(BlockState state) {
		super.blockState(blockState);
		return this;
	}

	@Override
	public NeoForgeSinglePosVirtualBlockGetter blockEntity(@Nullable BlockEntity blockEntity) {
		super.blockEntity(blockEntity);
		return this;
	}

	public NeoForgeSinglePosVirtualBlockGetter modelData(@Nullable ModelData modelData) {
		this.modelData = modelData;
		return this;
	}

	@Override
	public ModelData getModelData(BlockPos pos) {
		if (pos.equals(this.pos)) {
			return modelData != null ? modelData : super.getModelData(pos);
		}

		return super.getModelData(pos);
	}
}
