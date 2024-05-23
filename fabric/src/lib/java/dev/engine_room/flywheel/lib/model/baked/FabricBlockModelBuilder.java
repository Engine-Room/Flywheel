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
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class FabricBlockModelBuilder extends BlockModelBuilder {
	public FabricBlockModelBuilder(BlockState state) {
		super(state);
	}

	@Override
	public FabricBlockModelBuilder level(BlockAndTintGetter level) {
		super.level(level);
		return this;
	}

	@Override
	public FabricBlockModelBuilder poseStack(PoseStack poseStack) {
		super.poseStack(poseStack);
		return this;
	}

	@Override
	public FabricBlockModelBuilder materialFunc(BiFunction<RenderType, Boolean, Material> materialFunc) {
		super.materialFunc(materialFunc);
		return this;
	}

	@Override
	public SimpleModel build() {
		if (level == null) {
			level = VirtualEmptyBlockGetter.INSTANCE;
		}
		if (materialFunc == null) {
			materialFunc = ModelUtil::getMaterial;
		}

		var builder = ChunkLayerSortedListBuilder.<Model.ConfiguredMesh>getThreadLocal();

		BakedModelBufferer.bufferBlock(ModelUtil.VANILLA_RENDERER, level, state, poseStack, (renderType, shaded, data) -> {
			Material material = materialFunc.apply(renderType, shaded);
			if (material != null) {
				VertexView vertexView = new NoOverlayVertexView();
				MemoryBlock meshData = ModelUtil.convertVanillaBuffer(data, vertexView);
				var mesh = new SimpleMesh(vertexView, meshData, "source=BlockModelBuilder," + "blockState=" + state + ",renderType=" + renderType + ",shaded=" + shaded);
				builder.add(renderType, new Model.ConfiguredMesh(material, mesh));
			}
		});

		return new SimpleModel(builder.build());
	}
}
