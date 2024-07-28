package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;

public class FabricOriginBlockAndTintGetter extends OriginBlockAndTintGetter {
	@Nullable
	protected Object originRenderData;

	public FabricOriginBlockAndTintGetter(ToIntFunction<BlockPos> blockLightFunc, ToIntFunction<BlockPos> skyLightFunc) {
		super(blockLightFunc, skyLightFunc);
	}

	public void originRenderData(@Nullable Object renderData) {
		originRenderData = renderData;
	}

	@Override
	@Nullable
	public Object getBlockEntityRenderData(BlockPos pos) {
		if (pos.equals(BlockPos.ZERO)) {
			return originRenderData;
		}

		return super.getBlockEntityRenderData(pos);
	}
}
