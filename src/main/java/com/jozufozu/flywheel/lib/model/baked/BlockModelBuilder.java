package com.jozufozu.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.jozufozu.flywheel.lib.model.ModelUtil;
import com.jozufozu.flywheel.lib.model.SimpleMesh;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBufferer.ResultConsumer;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBufferer.ShadeSeparatedResultConsumer;
import com.jozufozu.flywheel.lib.vertex.VertexTypes;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class BlockModelBuilder {
	private final BlockState state;
	private boolean shadeSeparated = true;
	private BlockAndTintGetter renderWorld;
	private PoseStack poseStack;
	private ModelData modelData;
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

	public BlockModelBuilder modelData(ModelData modelData) {
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
			modelData = ModelData.EMPTY;
		}
		if (materialFunc == null) {
			materialFunc = ModelUtil::getMaterial;
		}

		ImmutableMap.Builder<Material, Mesh> meshMapBuilder = ImmutableMap.builder();

		if (shadeSeparated) {
			ShadeSeparatedResultConsumer resultConsumer = (renderType, shaded, data) -> {
				if (!data.isEmpty()) {
					Material material = materialFunc.apply(renderType, shaded);
					if (material != null) {
						MemoryBlock meshData = ModelUtil.convertVanillaBuffer(data, VertexTypes.BLOCK);
						meshMapBuilder.put(material, new SimpleMesh(VertexTypes.BLOCK, meshData, "source=BlockModelBuilder," + "blockState=" + state + ",renderType=" + renderType + ",shaded=" + shaded));
					}
				}
			};
			BakedModelBufferer.bufferBlockShadeSeparated(ModelUtil.VANILLA_RENDERER, renderWorld, state, poseStack, modelData, resultConsumer);
		} else {
			ResultConsumer resultConsumer = (renderType, data) -> {
				if (!data.isEmpty()) {
					Material material = materialFunc.apply(renderType, true);
					if (material != null) {
						MemoryBlock meshData = ModelUtil.convertVanillaBuffer(data, VertexTypes.BLOCK);
						meshMapBuilder.put(material, new SimpleMesh(VertexTypes.BLOCK, meshData, "source=BlockModelBuilder," + "blockState=" + state + ",renderType=" + renderType));
					}
				}
			};
			BakedModelBufferer.bufferBlock(ModelUtil.VANILLA_RENDERER, renderWorld, state, poseStack, modelData, resultConsumer);
		}

		return new TessellatedModel(meshMapBuilder.build(), shadeSeparated);
	}
}
