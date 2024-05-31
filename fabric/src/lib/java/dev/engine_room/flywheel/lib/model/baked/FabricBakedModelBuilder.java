package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.model.Mesh;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.model.ModelUtil;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class FabricBakedModelBuilder extends BakedModelBuilder {
	public FabricBakedModelBuilder(BakedModel bakedModel) {
		super(bakedModel);
	}

	@Override
	public FabricBakedModelBuilder level(BlockAndTintGetter level) {
		super.level(level);
		return this;
	}

	@Override
	public FabricBakedModelBuilder blockState(BlockState blockState) {
		super.blockState(blockState);
		return this;
	}

	@Override
	public FabricBakedModelBuilder poseStack(PoseStack poseStack) {
		super.poseStack(poseStack);
		return this;
	}

	@Override
	public FabricBakedModelBuilder materialFunc(BiFunction<RenderType, Boolean, Material> materialFunc) {
		super.materialFunc(materialFunc);
		return this;
	}

	@Override
	public SimpleModel build() {
		if (level == null) {
			level = VirtualEmptyBlockGetter.INSTANCE;
		}
		if (blockState == null) {
			blockState = Blocks.AIR.defaultBlockState();
		}
		if (materialFunc == null) {
			materialFunc = ModelUtil::getMaterial;
		}

		var builder = ChunkLayerSortedListBuilder.<Model.ConfiguredMesh>getThreadLocal();

		BakedModelBufferer.bufferSingle(ModelUtil.VANILLA_RENDERER.getModelRenderer(), level, bakedModel, blockState, poseStack, (renderType, shaded, data) -> {
			Material material = materialFunc.apply(renderType, shaded);
			if (material != null) {
				Mesh mesh = MeshHelper.blockVerticesToMesh(data, "source=BakedModelBuilder," + "bakedModel=" + bakedModel + ",renderType=" + renderType + ",shaded=" + shaded);
				builder.add(renderType, new Model.ConfiguredMesh(material, mesh));
			}
		});

		return new SimpleModel(builder.build());
	}
}
