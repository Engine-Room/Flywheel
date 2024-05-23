package com.jozufozu.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.vertex.VertexView;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.jozufozu.flywheel.lib.model.ModelUtil;
import com.jozufozu.flywheel.lib.model.SimpleMesh;
import com.jozufozu.flywheel.lib.model.SimpleModel;
import com.jozufozu.flywheel.lib.vertex.NoOverlayVertexView;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

public final class FabricMultiBlockModelBuilder extends MultiBlockModelBuilder {
	public FabricMultiBlockModelBuilder(BlockAndTintGetter level, Iterable<BlockPos> positions) {
		super(level, positions);
	}

	@Override
	public FabricMultiBlockModelBuilder poseStack(PoseStack poseStack) {
		super.poseStack(poseStack);
		return this;
	}

	@Override
	public FabricMultiBlockModelBuilder enableFluidRendering() {
		super.enableFluidRendering();
		return this;
	}

	@Override
	public FabricMultiBlockModelBuilder materialFunc(BiFunction<RenderType, Boolean, Material> materialFunc) {
		super.materialFunc(materialFunc);
		return this;
	}

	@Override
	public SimpleModel build() {
		if (materialFunc == null) {
			materialFunc = ModelUtil::getMaterial;
		}

		var builder = ChunkLayerSortedListBuilder.<Model.ConfiguredMesh>getThreadLocal();

		BakedModelBufferer.bufferMultiBlock(ModelUtil.VANILLA_RENDERER, positions.iterator(), level, poseStack, renderFluids, (renderType, shaded, data) -> {
			Material material = materialFunc.apply(renderType, shaded);
			if (material != null) {
				VertexView vertexView = new NoOverlayVertexView();
				MemoryBlock meshData = ModelUtil.convertVanillaBuffer(data, vertexView);
				var mesh = new SimpleMesh(vertexView, meshData, "source=MultiBlockModelBuilder," + "renderType=" + renderType + ",shaded=" + shaded);
				builder.add(renderType, new Model.ConfiguredMesh(material, mesh));
			}
		});

		return new SimpleModel(builder.build());
	}
}
