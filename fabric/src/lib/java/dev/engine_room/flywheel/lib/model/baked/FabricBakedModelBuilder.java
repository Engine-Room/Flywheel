package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.lib.model.ModelUtil;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class FabricBakedModelBuilder extends BakedModelBuilder {
	public FabricBakedModelBuilder(BakedModel bakedModel) {
		super(bakedModel);
	}

	@Override
	public FabricBakedModelBuilder level(@Nullable BlockAndTintGetter level) {
		super.level(level);
		return this;
	}

	@Override
	public FabricBakedModelBuilder pos(@Nullable BlockPos pos) {
		super.pos(pos);
		return this;
	}

	@Override
	public FabricBakedModelBuilder poseStack(@Nullable PoseStack poseStack) {
		super.poseStack(poseStack);
		return this;
	}

	@Override
	@Deprecated(forRemoval = true)
	public FabricBakedModelBuilder materialFunc(@Nullable BiFunction<RenderType, Boolean, @Nullable Material> materialFunc) {
		super.materialFunc(materialFunc);
		return this;
	}

	@Override
	public FabricBakedModelBuilder materialFunc(@Nullable BlockMaterialFunction materialFunc) {
		super.materialFunc(materialFunc);
		return this;
	}

	@Override
	public SimpleModel build() {
		if (level == null) {
			level = EmptyVirtualBlockGetter.FULL_DARK;
		}
		if (pos == null) {
			pos = BlockPos.ZERO;
		}
		if (materialFunc == null) {
			materialFunc = ModelUtil::getMaterial;
		}
		BlockState blockState = level.getBlockState(pos);

		ModelBuilderResultConsumer resultConsumer = new ModelBuilderResultConsumer(materialFunc);
		BakedModelBufferer.bufferModel(bakedModel, pos, level, blockState, poseStack, resultConsumer);
		return resultConsumer.build();
	}
}
