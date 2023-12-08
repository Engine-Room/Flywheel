package com.jozufozu.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.vertex.VertexView;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.jozufozu.flywheel.lib.model.ModelUtil;
import com.jozufozu.flywheel.lib.model.SimpleMesh;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBufferer.ResultConsumer;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBufferer.ShadeSeparatedResultConsumer;
import com.jozufozu.flywheel.lib.vertex.NoOverlayVertexView;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class BakedModelBuilder {
	private final BakedModel bakedModel;
	private boolean shadeSeparated = true;
	private BlockAndTintGetter renderWorld;
	private BlockState blockState;
	private PoseStack poseStack;
	private ModelData modelData;
	private BiFunction<RenderType, Boolean, Material> materialFunc;

	public BakedModelBuilder(BakedModel bakedModel) {
		this.bakedModel = bakedModel;
	}

	public BakedModelBuilder disableShadeSeparation() {
		shadeSeparated = false;
		return this;
	}

	public BakedModelBuilder renderWorld(BlockAndTintGetter renderWorld) {
		this.renderWorld = renderWorld;
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

	public BakedModelBuilder modelData(ModelData modelData) {
		this.modelData = modelData;
		return this;
	}

	public BakedModelBuilder materialFunc(BiFunction<RenderType, Boolean, Material> materialFunc) {
		this.materialFunc = materialFunc;
		return this;
	}

	public TessellatedModel build() {
		if (renderWorld == null) {
			renderWorld = VirtualEmptyBlockGetter.INSTANCE;
		}
		if (blockState == null) {
			blockState = Blocks.AIR.defaultBlockState();
		}
		if (modelData == null) {
			modelData = ModelData.EMPTY;
		}
		if (materialFunc == null) {
			materialFunc = ModelUtil::getMaterial;
		}

		ImmutableMap.Builder<Material, Mesh> meshMapBuilder = ImmutableMap.builder();

		if (shadeSeparated) {
			ShadeSeparatedResultConsumer resultConsumer = (renderType, shaded, data) -> {
				Material material = materialFunc.apply(renderType, shaded);
				if (material != null) {
					VertexView vertexView = new NoOverlayVertexView();
					MemoryBlock meshData = ModelUtil.convertVanillaBuffer(data, vertexView);
					meshMapBuilder.put(material, new SimpleMesh(vertexView, meshData, "source=BakedModelBuilder," + "bakedModel=" + bakedModel + ",renderType=" + renderType + ",shaded=" + shaded));
				}
			};
			BakedModelBufferer.bufferSingleShadeSeparated(ModelUtil.VANILLA_RENDERER.getModelRenderer(), renderWorld, bakedModel, blockState, poseStack, modelData, resultConsumer);
		} else {
			ResultConsumer resultConsumer = (renderType, data) -> {
				Material material = materialFunc.apply(renderType, true);
				if (material != null) {
					VertexView vertexView = new NoOverlayVertexView();
					MemoryBlock meshData = ModelUtil.convertVanillaBuffer(data, vertexView);
					meshMapBuilder.put(material, new SimpleMesh(vertexView, meshData, "source=BakedModelBuilder," + "bakedModel=" + bakedModel + ",renderType=" + renderType));
				}
			};
			BakedModelBufferer.bufferSingle(ModelUtil.VANILLA_RENDERER.getModelRenderer(), renderWorld, bakedModel, blockState, poseStack, modelData, resultConsumer);
		}

		return new TessellatedModel(meshMapBuilder.build(), shadeSeparated);
	}
}
