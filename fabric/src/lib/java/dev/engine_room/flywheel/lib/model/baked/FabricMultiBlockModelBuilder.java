package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.model.Mesh;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.model.ModelUtil;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

public final class FabricMultiBlockModelBuilder extends MultiBlockModelBuilder {
	public FabricMultiBlockModelBuilder(BlockAndTintGetter level, Iterable<BlockPos> positions) {
		super(level, positions);
	}

	@Override
	public FabricMultiBlockModelBuilder poseStack(@Nullable PoseStack poseStack) {
		super.poseStack(poseStack);
		return this;
	}

	@Override
	public FabricMultiBlockModelBuilder renderFluids(boolean renderFluids) {
		super.renderFluids(renderFluids);
		return this;
	}

	@Override
	public FabricMultiBlockModelBuilder materialFunc(@Nullable BiFunction<RenderType, Boolean, Material> materialFunc) {
		super.materialFunc(materialFunc);
		return this;
	}

	@Override
	public SimpleModel build() {
		if (materialFunc == null) {
			materialFunc = ModelUtil::getMaterial;
		}

		var builder = ChunkLayerSortedListBuilder.<Model.ConfiguredMesh>getThreadLocal();

		BakedModelBufferer.bufferMultiBlock(positions.iterator(), level, poseStack, renderFluids, (renderType, shaded, data) -> {
			Material material = materialFunc.apply(renderType, shaded);
			if (material != null) {
				Mesh mesh = MeshHelper.blockVerticesToMesh(data, "source=MultiBlockModelBuilder," + "renderType=" + renderType + ",shaded=" + shaded);
				builder.add(renderType, new Model.ConfiguredMesh(material, mesh));
			}
		});

		return new SimpleModel(builder.build());
	}
}
