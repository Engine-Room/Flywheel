package com.jozufozu.flywheel.lib.model.baked;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.vertex.VertexView;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.jozufozu.flywheel.lib.model.ModelUtil;
import com.jozufozu.flywheel.lib.model.SimpleMesh;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBufferer.ResultConsumer;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBufferer.ShadeSeparatedResultConsumer;
import com.jozufozu.flywheel.lib.vertex.NoOverlayVertexView;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraftforge.client.model.data.ModelData;

public class MultiBlockModelBuilder {
	private final BlockAndTintGetter renderWorld;
	private final Iterable<BlockPos> positions;
	private PoseStack poseStack;
	private Function<BlockPos, ModelData> modelDataLookup;
	private boolean renderFluids = false;
	private boolean shadeSeparated = true;
	private BiFunction<RenderType, Boolean, Material> materialFunc;

	public MultiBlockModelBuilder(BlockAndTintGetter renderWorld, Iterable<BlockPos> positions) {
		this.renderWorld = renderWorld;
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

	public MultiBlockModelBuilder disableShadeSeparation() {
		shadeSeparated = false;
		return this;
	}

	public MultiBlockModelBuilder materialFunc(BiFunction<RenderType, Boolean, Material> materialFunc) {
		this.materialFunc = materialFunc;
		return this;
	}

	public TessellatedModel build() {
		if (modelDataLookup == null) {
			modelDataLookup = pos -> ModelData.EMPTY;
		}
		if (materialFunc == null) {
			materialFunc = ModelUtil::getMaterial;
		}

		var out = ImmutableList.<Model.ConfiguredMesh>builder();

		if (shadeSeparated) {
			ShadeSeparatedResultConsumer resultConsumer = (renderType, shaded, data) -> {
				Material material = materialFunc.apply(renderType, shaded);
				if (material != null) {
					VertexView vertexView = new NoOverlayVertexView();
					MemoryBlock meshData = ModelUtil.convertVanillaBuffer(data, vertexView);
					var mesh = new SimpleMesh(vertexView, meshData, "source=MultiBlockModelBuilder," + "renderType=" + renderType + ",shaded=" + shaded);
					out.add(new Model.ConfiguredMesh(material, mesh));
				}
			};
			BakedModelBufferer.bufferMultiBlockShadeSeparated(ModelUtil.VANILLA_RENDERER, positions.iterator(), renderWorld, poseStack, modelDataLookup, renderFluids, resultConsumer);
		} else {
			ResultConsumer resultConsumer = (renderType, data) -> {
				Material material = materialFunc.apply(renderType, true);
				if (material != null) {
					VertexView vertexView = new NoOverlayVertexView();
					MemoryBlock meshData = ModelUtil.convertVanillaBuffer(data, vertexView);
					var mesh = new SimpleMesh(vertexView, meshData, "source=MultiBlockModelBuilder," + "renderType=" + renderType);
					out.add(new Model.ConfiguredMesh(material, mesh));
				}
			};
			BakedModelBufferer.bufferMultiBlock(ModelUtil.VANILLA_RENDERER, positions.iterator(), renderWorld, poseStack, modelDataLookup, renderFluids, resultConsumer);
		}

		return new TessellatedModel(out.build(), shadeSeparated);
	}
}
