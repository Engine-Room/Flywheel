package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.lib.internal.FlwLibXplat;
import dev.engine_room.flywheel.lib.model.ModelUtil;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;

public final class BlockModelBuilder {
	final BlockStateModel blockModel;
	@Nullable
	BlockAndTintGetter level;
	@Nullable
	BlockPos pos;
	@Nullable
	PoseStack poseStack;
	@Nullable
	BlockMaterialFunction materialFunc;

	public BlockModelBuilder(BlockStateModel blockModel) {
		this.blockModel = blockModel;
	}

	public BlockModelBuilder level(@Nullable BlockAndTintGetter level) {
		this.level = level;
		return this;
	}

	public BlockModelBuilder pos(@Nullable BlockPos pos) {
		this.pos = pos;
		return this;
	}

	public BlockModelBuilder poseStack(@Nullable PoseStack poseStack) {
		this.poseStack = poseStack;
		return this;
	}

	@Deprecated(forRemoval = true)
	public BlockModelBuilder materialFunc(@Nullable BiFunction<ChunkSectionLayer, Boolean, @Nullable Material> materialFunc) {
		if (materialFunc != null) {
			this.materialFunc = (chunkSectionLayer, shaded, ambientOcclusion) -> materialFunc.apply(chunkSectionLayer, shaded);
		} else {
			this.materialFunc = null;
		}
		return this;
	}

	public BlockModelBuilder materialFunc(@Nullable BlockMaterialFunction materialFunc) {
		this.materialFunc = materialFunc;
		return this;
	}

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

		return FlwLibXplat.INSTANCE.buildBlockModelBuilder(this);
	}
}
