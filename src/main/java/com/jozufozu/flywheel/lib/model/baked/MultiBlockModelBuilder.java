package com.jozufozu.flywheel.lib.model.baked;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.vertex.VertexView;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.jozufozu.flywheel.lib.model.ModelUtil;
import com.jozufozu.flywheel.lib.model.SimpleMesh;
import com.jozufozu.flywheel.lib.model.SimpleModel;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBufferer.ResultConsumer;
import com.jozufozu.flywheel.lib.vertex.NoOverlayVertexView;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraftforge.client.model.data.ModelData;

public class MultiBlockModelBuilder {
	private final BlockAndTintGetter level;
	private final Iterable<BlockPos> positions;
	@Nullable
	private PoseStack poseStack;
	@Nullable
	private Function<BlockPos, ModelData> modelDataLookup;
	private boolean renderFluids = false;
	@Nullable
	private BiFunction<RenderType, Boolean, Material> materialFunc;

	public MultiBlockModelBuilder(BlockAndTintGetter level, Iterable<BlockPos> positions) {
		this.level = level;
		this.positions = positions;
	}

	public MultiBlockModelBuilder poseStack(PoseStack poseStack) {
		this.poseStack = poseStack;
		return this;
	}

	public MultiBlockModelBuilder modelDataLookup(Function<BlockPos, ModelData> modelDataLookup) {
		this.modelDataLookup = modelDataLookup;
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

	public SimpleModel build() {
		if (modelDataLookup == null) {
			modelDataLookup = pos -> ModelData.EMPTY;
		}
		if (materialFunc == null) {
			materialFunc = ModelUtil::getMaterial;
		}

		var out = ImmutableList.<Model.ConfiguredMesh>builder();

		ResultConsumer resultConsumer = (renderType, shaded, data) -> {
			Material material = materialFunc.apply(renderType, shaded);
			if (material != null) {
				VertexView vertexView = new NoOverlayVertexView();
				MemoryBlock meshData = ModelUtil.convertVanillaBuffer(data, vertexView);
				var mesh = new SimpleMesh(vertexView, meshData, "source=MultiBlockModelBuilder," + "renderType=" + renderType + ",shaded=" + shaded);
				out.add(new Model.ConfiguredMesh(material, mesh));
			}
		};
		BakedModelBufferer.bufferMultiBlock(ModelUtil.VANILLA_RENDERER, positions.iterator(), level, poseStack, modelDataLookup, renderFluids, resultConsumer);

		return new SimpleModel(out.build());
	}
}
