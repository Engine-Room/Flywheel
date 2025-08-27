package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.lib.internal.FlwLibXplat;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

@ApiStatus.NonExtendable
public abstract class BakedModelBuilder {
	final BakedModel bakedModel;
	@Nullable
	BlockAndTintGetter level;
	@Nullable
	BlockPos pos;
	@Nullable
	PoseStack poseStack;
	@Nullable
	BlockMaterialFunction materialFunc;

	BakedModelBuilder(BakedModel bakedModel) {
		this.bakedModel = bakedModel;
	}

	public static BakedModelBuilder create(BakedModel bakedModel) {
		return FlwLibXplat.INSTANCE.createBakedModelBuilder(bakedModel);
	}

	public BakedModelBuilder level(@Nullable BlockAndTintGetter level) {
		this.level = level;
		return this;
	}

	public BakedModelBuilder pos(@Nullable BlockPos pos) {
		this.pos = pos;
		return this;
	}

	public BakedModelBuilder poseStack(@Nullable PoseStack poseStack) {
		this.poseStack = poseStack;
		return this;
	}

	@Deprecated(forRemoval = true)
	public BakedModelBuilder materialFunc(@Nullable BiFunction<RenderType, Boolean, @Nullable Material> materialFunc) {
		if (materialFunc != null) {
			this.materialFunc = (chunkRenderType, shaded, ambientOcclusion) -> materialFunc.apply(chunkRenderType, shaded);
		} else {
			this.materialFunc = null;
		}
		return this;
	}

	public BakedModelBuilder materialFunc(@Nullable BlockMaterialFunction materialFunc) {
		this.materialFunc = materialFunc;
		return this;
	}

	public abstract SimpleModel build();
}
