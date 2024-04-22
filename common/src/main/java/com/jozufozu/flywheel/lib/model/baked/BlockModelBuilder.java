package com.jozufozu.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.lib.model.SimpleModel;
import com.jozufozu.flywheel.platform.ClientPlatform;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BlockModelBuilder {
	protected final BlockState state;
	@Nullable
	protected BlockAndTintGetter level;
	@Nullable
	protected PoseStack poseStack;
	@Nullable
	protected BiFunction<RenderType, Boolean, Material> materialFunc;

	public static BlockModelBuilder create(BlockState state) {
		return ClientPlatform.INSTANCE.blockModelBuilder(state);
	}

	protected BlockModelBuilder(BlockState state) {
		this.state = state;
	}

	public BlockModelBuilder level(BlockAndTintGetter level) {
		this.level = level;
		return this;
	}

	public BlockModelBuilder poseStack(PoseStack poseStack) {
		this.poseStack = poseStack;
		return this;
	}

	public BlockModelBuilder materialFunc(BiFunction<RenderType, Boolean, Material> materialFunc) {
		this.materialFunc = materialFunc;
		return this;
	}

	public abstract SimpleModel build();
}
