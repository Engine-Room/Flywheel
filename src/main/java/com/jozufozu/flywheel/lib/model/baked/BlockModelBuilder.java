package com.jozufozu.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.jozufozu.flywheel.lib.model.ModelUtil;
import com.jozufozu.flywheel.lib.model.SimpleMesh;
import com.jozufozu.flywheel.lib.model.baked.ModelBufferingUtil.ResultConsumer;
import com.jozufozu.flywheel.lib.model.baked.ModelBufferingUtil.ShadeSeparatedResultConsumer;
import com.jozufozu.flywheel.lib.vertex.VertexTypes;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;

public class BlockModelBuilder {
	private final BlockState state;
	private boolean shadeSeparated = true;
	private BlockAndTintGetter renderWorld;
	private PoseStack poseStack;
	private IModelData modelData;
	private BiFunction<RenderType, Boolean, Material> materialFunc;

	public BlockModelBuilder(BlockState state) {
		this.state = state;
	}

	public BlockModelBuilder disableShadeSeparation() {
		shadeSeparated = false;
		return this;
	}

	public BlockModelBuilder renderWorld(BlockAndTintGetter renderWorld) {
		this.renderWorld = renderWorld;
		return this;
	}

	public BlockModelBuilder poseStack(PoseStack poseStack) {
		this.poseStack = poseStack;
		return this;
	}

	public BlockModelBuilder modelData(IModelData modelData) {
		this.modelData = modelData;
		return this;
	}

	public BlockModelBuilder materialFunc(BiFunction<RenderType, Boolean, Material> materialFunc) {
		this.materialFunc = materialFunc;
		return this;
	}

	public TessellatedModel build() {
		if (renderWorld == null) {
			renderWorld = VirtualEmptyBlockGetter.INSTANCE;
		}
		if (modelData == null) {
			modelData = VirtualEmptyModelData.INSTANCE;
		}
		if (materialFunc == null) {
			materialFunc = ModelUtil::getMaterial;
		}

		ImmutableMap.Builder<Material, Mesh> meshMapBuilder = ImmutableMap.builder();

		if (shadeSeparated) {
			ShadeSeparatedResultConsumer resultConsumer = (renderType, shaded, data) -> {
				if (!ModelUtil.isVanillaBufferEmpty(data)) {
					Material material = materialFunc.apply(renderType, shaded);
					if (material != null) {
						MemoryBlock meshData = ModelUtil.convertVanillaBuffer(data, VertexTypes.BLOCK);
						meshMapBuilder.put(material, new SimpleMesh(VertexTypes.BLOCK, meshData, "state=" + state.toString() + ",renderType=" + renderType.toString() + ",shaded=" + shaded));
					}
				}
			};
			ModelBufferingUtil.bufferBlockShadeSeparated(ModelUtil.VANILLA_RENDERER, renderWorld, state, poseStack, modelData, resultConsumer);
		} else {
			ResultConsumer resultConsumer = (renderType, data) -> {
				if (!ModelUtil.isVanillaBufferEmpty(data)) {
					Material material = materialFunc.apply(renderType, true);
					if (material != null) {
						MemoryBlock meshData = ModelUtil.convertVanillaBuffer(data, VertexTypes.BLOCK);
						meshMapBuilder.put(material, new SimpleMesh(VertexTypes.BLOCK, meshData, "state=" + state.toString() + ",renderType=" + renderType.toString()));
					}
				}
			};
			ModelBufferingUtil.bufferBlock(ModelUtil.VANILLA_RENDERER, renderWorld, state, poseStack, modelData, resultConsumer);
		}

		return new TessellatedModel(meshMapBuilder.build(), shadeSeparated);
	}
}
