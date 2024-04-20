package com.jozufozu.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.vertex.VertexView;
import com.jozufozu.flywheel.lib.internal.FlywheelLibPlatform;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.jozufozu.flywheel.lib.model.ModelUtil;
import com.jozufozu.flywheel.lib.model.SimpleMesh;
import com.jozufozu.flywheel.lib.model.SimpleModel;
import com.jozufozu.flywheel.lib.model.baked.MeshEmitter.ResultConsumer;
import com.jozufozu.flywheel.lib.vertex.NoOverlayVertexView;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BakedModelBuilder {
	protected final BakedModel bakedModel;
	@Nullable
	protected BlockAndTintGetter level;
	@Nullable
	protected BlockState blockState;
	@Nullable
	protected PoseStack poseStack;
	@Nullable
	protected BiFunction<RenderType, Boolean, Material> materialFunc;

	public static BakedModelBuilder create(BakedModel bakedModel) {
		return FlywheelLibPlatform.INSTANCE.bakedModelBuilder(bakedModel);
	}

	protected BakedModelBuilder(BakedModel bakedModel) {
		this.bakedModel = bakedModel;
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
