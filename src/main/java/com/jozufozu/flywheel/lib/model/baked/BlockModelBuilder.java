package com.jozufozu.flywheel.lib.model.baked;

import java.util.function.BiFunction;

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
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class BlockModelBuilder {
	private final BlockState state;
	private BlockAndTintGetter renderWorld;
	private PoseStack poseStack;
	private ModelData modelData;
	private boolean shadeSeparated = true;
	private BiFunction<RenderType, Boolean, Material> materialFunc;

	public BlockModelBuilder(BlockState state) {
		this.state = state;
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

	public BlockModelBuilder disableShadeSeparation() {
		shadeSeparated = false;
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

		var out = ImmutableList.<Model.ConfiguredMesh>builder();

		if (shadeSeparated) {
			ShadeSeparatedResultConsumer resultConsumer = (renderType, shaded, data) -> {
				Material material = materialFunc.apply(renderType, shaded);
				if (material != null) {
					VertexView vertexView = new NoOverlayVertexView();
					MemoryBlock meshData = ModelUtil.convertVanillaBuffer(data, vertexView);
					var mesh = new SimpleMesh(vertexView, meshData, "source=BlockModelBuilder," + "blockState=" + state + ",renderType=" + renderType + ",shaded=" + shaded);
					out.add(new Model.ConfiguredMesh(material, mesh));
				}
			};
			BakedModelBufferer.bufferBlockShadeSeparated(ModelUtil.VANILLA_RENDERER, renderWorld, state, poseStack, modelData, resultConsumer);
		} else {
			ResultConsumer resultConsumer = (renderType, data) -> {
				Material material = materialFunc.apply(renderType, true);
				if (material != null) {
					VertexView vertexView = new NoOverlayVertexView();
					MemoryBlock meshData = ModelUtil.convertVanillaBuffer(data, vertexView);
					var mesh = new SimpleMesh(vertexView, meshData, "source=BlockModelBuilder," + "blockState=" + state + ",renderType=" + renderType);
					out.add(new Model.ConfiguredMesh(material, mesh));
				}
			};
			BakedModelBufferer.bufferBlock(ModelUtil.VANILLA_RENDERER, renderWorld, state, poseStack, modelData, resultConsumer);
		}

		return new TessellatedModel(out.build(), shadeSeparated);
	}
}
