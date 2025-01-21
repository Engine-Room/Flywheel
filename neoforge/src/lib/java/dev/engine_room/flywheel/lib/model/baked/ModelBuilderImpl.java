package dev.engine_room.flywheel.lib.model.baked;

import org.jetbrains.annotations.ApiStatus;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.model.Mesh;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.model.SimpleModel;

@ApiStatus.Internal
public final class ModelBuilderImpl {
	private ModelBuilderImpl() {
	}

	public static SimpleModel buildBakedModelBuilder(BakedModelBuilder builder) {
		var builder1 = ChunkLayerSortedListBuilder.<Model.ConfiguredMesh>getThreadLocal();

		BakedModelBufferer.bufferSingle(builder.level, builder.bakedModel, builder.blockState, builder.poseStack, (renderType, shaded, data) -> {
			Material material = builder.materialFunc.apply(renderType, shaded);
			if (material != null) {
				Mesh mesh = MeshHelper.blockVerticesToMesh(data, "source=BakedModelBuilder," + "bakedModel=" + builder.bakedModel + ",renderType=" + renderType + ",shaded=" + shaded);
				builder1.add(renderType, new Model.ConfiguredMesh(material, mesh));
			}
		});

		return new SimpleModel(builder1.build());
	}

	public static SimpleModel buildBlockModelBuilder(BlockModelBuilder builder) {
		var builder1 = ChunkLayerSortedListBuilder.<Model.ConfiguredMesh>getThreadLocal();

		BakedModelBufferer.bufferBlock(builder.level, builder.state, builder.poseStack, (renderType, shaded, data) -> {
			Material material = builder.materialFunc.apply(renderType, shaded);
			if (material != null) {
				Mesh mesh = MeshHelper.blockVerticesToMesh(data, "source=BlockModelBuilder," + "blockState=" + builder.state + ",renderType=" + renderType + ",shaded=" + shaded);
				builder1.add(renderType, new Model.ConfiguredMesh(material, mesh));
			}
		});

		return new SimpleModel(builder1.build());
	}

	public static SimpleModel buildMultiBlockModelBuilder(MultiBlockModelBuilder builder) {
		var builder1 = ChunkLayerSortedListBuilder.<Model.ConfiguredMesh>getThreadLocal();

		BakedModelBufferer.bufferMultiBlock(builder.positions.iterator(), builder.level, builder.poseStack, builder.renderFluids, (renderType, shaded, data) -> {
			Material material = builder.materialFunc.apply(renderType, shaded);
			if (material != null) {
				Mesh mesh = MeshHelper.blockVerticesToMesh(data, "source=MultiBlockModelBuilder," + "renderType=" + renderType + ",shaded=" + shaded);
				builder1.add(renderType, new Model.ConfiguredMesh(material, mesh));
			}
		});

		return new SimpleModel(builder1.build());
	}
}
