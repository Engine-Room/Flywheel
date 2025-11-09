package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.lib.model.ModelUtil;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

public final class FabricBlockModelBuilder extends BlockModelBuilder {
	public FabricBlockModelBuilder(BlockAndTintGetter level, Iterable<BlockPos> positions) {
		super(level, positions);
	}

	@Override
	public FabricBlockModelBuilder poseStack(@Nullable PoseStack poseStack) {
		super.poseStack(poseStack);
		return this;
	}

	@Override
	public FabricBlockModelBuilder renderFluids(boolean renderFluids) {
		super.renderFluids(renderFluids);
		return this;
	}

	@Override
	@Deprecated(forRemoval = true)
	public FabricBlockModelBuilder materialFunc(@Nullable BiFunction<RenderType, Boolean, @Nullable Material> materialFunc) {
		super.materialFunc(materialFunc);
		return this;
	}

	@Override
	public FabricBlockModelBuilder materialFunc(@Nullable BlockMaterialFunction materialFunc) {
		super.materialFunc(materialFunc);
		return this;
	}

	@Override
	public SimpleModel build() {
		if (materialFunc == null) {
			materialFunc = ModelUtil::getMaterial;
		}

		return BakedModelBufferer.bufferBlocks(positions.iterator(), level, poseStack, renderFluids, materialFunc);
	}
}
