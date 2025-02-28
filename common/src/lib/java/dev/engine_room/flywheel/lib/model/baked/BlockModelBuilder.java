package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.lib.internal.FlwLibXplat;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

@ApiStatus.NonExtendable
public abstract class BlockModelBuilder {
	final BlockAndTintGetter level;
	final Iterable<BlockPos> positions;
	@Nullable
	PoseStack poseStack;
	boolean renderFluids = false;
	@Nullable
	BiFunction<RenderType, Boolean, Material> materialFunc;

	BlockModelBuilder(BlockAndTintGetter level, Iterable<BlockPos> positions) {
		this.level = level;
		this.positions = positions;
	}

	public static BlockModelBuilder create(BlockAndTintGetter level, Iterable<BlockPos> positions) {
		return FlwLibXplat.INSTANCE.createBlockModelBuilder(level, positions);
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

	public abstract SimpleModel build();
}
