package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.vertex.VertexView;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;
import dev.engine_room.flywheel.lib.model.ModelUtil;
import dev.engine_room.flywheel.lib.model.SimpleMesh;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import dev.engine_room.flywheel.lib.vertex.NoOverlayVertexView;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public final class ForgeBakedModelBuilder extends BakedModelBuilder {
	@Nullable
	private ModelData modelData;

	public ForgeBakedModelBuilder(BakedModel bakedModel) {
		super(bakedModel);
	}

	@Override
	public ForgeBakedModelBuilder level(BlockAndTintGetter level) {
		super.level(level);
		return this;
	}

	@Override
	public ForgeBakedModelBuilder blockState(BlockState blockState) {
		super.blockState(blockState);
		return this;
	}

	@Override
	public ForgeBakedModelBuilder poseStack(PoseStack poseStack) {
		super.poseStack(poseStack);
		return this;
	}

	@Override
	public ForgeBakedModelBuilder materialFunc(BiFunction<RenderType, Boolean, Material> materialFunc) {
		super.materialFunc(materialFunc);
		return this;
	}

	public ForgeBakedModelBuilder modelData(ModelData modelData) {
		this.modelData = modelData;
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
		if (modelData == null) {
			modelData = ModelData.EMPTY;
		}

		var builder = ChunkLayerSortedListBuilder.<Model.ConfiguredMesh>getThreadLocal();

		BakedModelBufferer.bufferSingle(ModelUtil.VANILLA_RENDERER.getModelRenderer(), level, bakedModel, blockState, poseStack, modelData, (renderType, shaded, data) -> {
			Material material = materialFunc.apply(renderType, shaded);
			if (material != null) {
				VertexView vertexView = new NoOverlayVertexView();
				MemoryBlock meshData = ModelUtil.convertVanillaBuffer(data, vertexView);
				var mesh = new SimpleMesh(vertexView, meshData, "source=BakedModelBuilder," + "bakedModel=" + bakedModel + ",renderType=" + renderType + ",shaded=" + shaded);
				builder.add(renderType, new Model.ConfiguredMesh(material, mesh));
			}
		});

		return new SimpleModel(builder.build());
	}
}
