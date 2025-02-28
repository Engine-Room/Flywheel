package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.lib.internal.FlwLibXplat;
import dev.engine_room.flywheel.lib.model.ModelUtil;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

public final class BlockModelBuilder {
	final BlockAndTintGetter level;
	final Iterable<BlockPos> positions;
	@Nullable
	PoseStack poseStack;
	boolean renderFluids = false;
	@Nullable
	BiFunction<RenderType, Boolean, Material> materialFunc;

	public BlockModelBuilder(BlockAndTintGetter level, Iterable<BlockPos> positions) {
		this.level = level;
		this.positions = positions;
	}

	public BlockModelBuilder poseStack(@Nullable PoseStack poseStack) {
		this.poseStack = poseStack;
		return this;
	}

	public BlockModelBuilder renderFluids(boolean renderFluids) {
		this.renderFluids = renderFluids;
		return this;
	}

	public BlockModelBuilder materialFunc(@Nullable BiFunction<RenderType, Boolean, Material> materialFunc) {
		this.materialFunc = materialFunc;
		return this;
	}

	public SimpleModel build() {
		if (materialFunc == null) {
			materialFunc = ModelUtil::getMaterial;
		}

		return FlwLibXplat.INSTANCE.buildBlockModelBuilder(this);
	}
}
