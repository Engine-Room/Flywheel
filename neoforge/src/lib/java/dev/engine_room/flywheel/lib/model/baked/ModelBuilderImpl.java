package dev.engine_room.flywheel.lib.model.baked;

import org.jetbrains.annotations.ApiStatus;

import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.world.level.block.state.BlockState;

// Forward the XplatImpl calls here so we can access the package private fields.
@ApiStatus.Internal
public final class ModelBuilderImpl {
	private ModelBuilderImpl() {
	}

	public static SimpleModel buildBlockModelBuilder(BlockModelBuilder builder) {
		BlockState blockState = builder.level.getBlockState(builder.pos);

		return BlockStateModelBufferer.bufferModel(builder.blockModel, builder.pos, builder.level, blockState, builder.poseStack, builder.materialFunc);
	}

	public static SimpleModel buildLevelModelBuilder(LevelModelBuilder builder) {
		return BlockStateModelBufferer.bufferBlocks(builder.positions.iterator(), builder.level, builder.poseStack, builder.renderFluids, builder.materialFunc);
	}
}
