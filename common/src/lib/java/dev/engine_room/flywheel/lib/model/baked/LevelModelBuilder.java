package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.lib.internal.FlwLibXplat;
import dev.engine_room.flywheel.lib.model.ModelUtil;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

public final class LevelModelBuilder {
	final BlockAndTintGetter level;
	final Iterable<BlockPos> positions;
	@Nullable
	PoseStack poseStack;
	boolean renderFluids = false;
	@Nullable
	BlockMaterialFunction materialFunc;

	public LevelModelBuilder(BlockAndTintGetter level, Iterable<BlockPos> positions) {
		this.level = level;
		this.positions = positions;
	}

	public LevelModelBuilder poseStack(@Nullable PoseStack poseStack) {
		this.poseStack = poseStack;
		return this;
	}

	public LevelModelBuilder renderFluids(boolean renderFluids) {
		this.renderFluids = renderFluids;
		return this;
	}

	@Deprecated(forRemoval = true)
	public LevelModelBuilder materialFunc(@Nullable BiFunction<ChunkSectionLayer, Boolean, @Nullable Material> materialFunc) {
		if (materialFunc != null) {
			this.materialFunc = (chunkRenderType, shaded, ambientOcclusion) -> materialFunc.apply(chunkRenderType, shaded);
		} else {
			this.materialFunc = null;
		}
		return this;
	}

	public LevelModelBuilder materialFunc(@Nullable BlockMaterialFunction materialFunc) {
		this.materialFunc = materialFunc;
		return this;
	}

	public SimpleModel build() {
		if (materialFunc == null) {
			materialFunc = ModelUtil::getMaterial;
		}

		return FlwLibXplat.INSTANCE.buildLevelModelBuilder(this);
	}
}
