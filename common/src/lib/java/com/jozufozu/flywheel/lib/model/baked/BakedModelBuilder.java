package com.jozufozu.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.lib.internal.FlwLibXplat;
import com.jozufozu.flywheel.lib.model.SimpleModel;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

@ApiStatus.NonExtendable
public abstract class BakedModelBuilder {
	final BakedModel bakedModel;
	@Nullable
	BlockAndTintGetter level;
	@Nullable
	BlockState blockState;
	@Nullable
	PoseStack poseStack;
	@Nullable
	BiFunction<RenderType, Boolean, Material> materialFunc;

	BakedModelBuilder(BakedModel bakedModel) {
		this.bakedModel = bakedModel;
	}

	public static BakedModelBuilder create(BakedModel bakedModel) {
		return FlwLibXplat.INSTANCE.createBakedModelBuilder(bakedModel);
	}

	public BakedModelBuilder level(BlockAndTintGetter level) {
		this.level = level;
		return this;
	}

	public BakedModelBuilder blockState(BlockState blockState) {
		this.blockState = blockState;
		return this;
	}

	public BakedModelBuilder poseStack(PoseStack poseStack) {
		this.poseStack = poseStack;
		return this;
	}

	public BakedModelBuilder materialFunc(BiFunction<RenderType, Boolean, Material> materialFunc) {
		this.materialFunc = materialFunc;
		return this;
	}

	public abstract SimpleModel build();
}
