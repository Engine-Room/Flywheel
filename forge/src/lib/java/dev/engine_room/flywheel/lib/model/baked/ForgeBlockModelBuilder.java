package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.lib.model.ModelUtil;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.data.ModelData;

public final class ForgeBlockModelBuilder extends BlockModelBuilder {
	@Nullable
	private Function<BlockPos, ModelData> modelDataLookup;

	public ForgeBlockModelBuilder(BlockAndTintGetter level, Iterable<BlockPos> positions) {
		super(level, positions);
	}

	@Override
	public ForgeBlockModelBuilder poseStack(@Nullable PoseStack poseStack) {
		super.poseStack(poseStack);
		return this;
	}

	@Override
	public ForgeBlockModelBuilder renderFluids(boolean renderFluids) {
		super.renderFluids(renderFluids);
		return this;
	}

	@Override
	@Deprecated(forRemoval = true)
	public ForgeBlockModelBuilder materialFunc(@Nullable BiFunction<RenderType, Boolean, @Nullable Material> materialFunc) {
		super.materialFunc(materialFunc);
		return this;
	}

	@Override
	public ForgeBlockModelBuilder materialFunc(@Nullable BlockMaterialFunction materialFunc) {
		super.materialFunc(materialFunc);
		return this;
	}

	public ForgeBlockModelBuilder modelDataLookup(@Nullable Function<BlockPos, ModelData> modelDataLookup) {
		this.modelDataLookup = modelDataLookup;
		return this;
	}

	@Override
	public SimpleModel build() {
		if (materialFunc == null) {
			materialFunc = ModelUtil::getMaterial;
		}
		if (modelDataLookup == null) {
			modelDataLookup = pos -> {
				BlockEntity blockEntity = level.getBlockEntity(pos);
				return blockEntity != null ? blockEntity.getModelData() : ModelData.EMPTY;
			};
		}

		ModelBuilderResultConsumer resultConsumer = new ModelBuilderResultConsumer(materialFunc);
		BakedModelBufferer.bufferBlocks(positions.iterator(), level, poseStack, modelDataLookup, renderFluids, resultConsumer);
		return resultConsumer.build();
	}
}
