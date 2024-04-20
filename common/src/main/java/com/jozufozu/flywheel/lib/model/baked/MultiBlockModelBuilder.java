package com.jozufozu.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.lib.internal.FlywheelLibPlatform;
import com.jozufozu.flywheel.lib.model.SimpleModel;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

public abstract class MultiBlockModelBuilder {
	protected final BlockAndTintGetter level;
	protected final Iterable<BlockPos> positions;
	@Nullable
	protected PoseStack poseStack;
	protected boolean renderFluids = false;
	@Nullable
	protected BiFunction<RenderType, Boolean, Material> materialFunc;

	public static MultiBlockModelBuilder create(BlockAndTintGetter level, Iterable<BlockPos> positions) {
		return FlywheelLibPlatform.INSTANCE.multiBlockModelBuilder(level, positions);
	}

	protected MultiBlockModelBuilder(BlockAndTintGetter level, Iterable<BlockPos> positions) {
		this.level = level;
		this.positions = positions;
	}

	public MultiBlockModelBuilder poseStack(PoseStack poseStack) {
		this.poseStack = poseStack;
		return this;
	}

	public MultiBlockModelBuilder enableFluidRendering() {
		renderFluids = true;
		return this;
	}

	public MultiBlockModelBuilder materialFunc(BiFunction<RenderType, Boolean, Material> materialFunc) {
		this.materialFunc = materialFunc;
		return this;
	}

	public abstract SimpleModel build();
}
