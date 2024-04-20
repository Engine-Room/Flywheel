package com.jozufozu.flywheel.lib.model.baked;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.vertex.VertexView;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.jozufozu.flywheel.lib.model.ModelUtil;
import com.jozufozu.flywheel.lib.model.SimpleMesh;
import com.jozufozu.flywheel.lib.model.SimpleModel;
import com.jozufozu.flywheel.lib.model.baked.MeshEmitter.ResultConsumer;
import com.jozufozu.flywheel.lib.vertex.NoOverlayVertexView;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class ForgeBlockModelBuilder extends BlockModelBuilder {
	@Nullable
	private ModelData modelData;

	public ForgeBlockModelBuilder(BlockState state) {
		super(state);
	}

	public ForgeBlockModelBuilder modelData(ModelData modelData) {
		this.modelData = modelData;
		return this;
	}

	public SimpleModel build() {
		if (level == null) {
			level = VirtualEmptyBlockGetter.INSTANCE;
		}
		if (modelData == null) {
			modelData = ModelData.EMPTY;
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
				var mesh = new SimpleMesh(vertexView, meshData, "source=BlockModelBuilder," + "blockState=" + state + ",renderType=" + renderType + ",shaded=" + shaded);
				out.add(new Model.ConfiguredMesh(material, mesh));
			}
		};
		BakedModelBufferer.bufferBlock(ModelUtil.VANILLA_RENDERER, level, state, poseStack, modelData, resultConsumer);

		return new SimpleModel(out.build());
	}
}
