package dev.engine_room.flywheel.impl;

import org.jetbrains.annotations.UnknownNullability;

import dev.engine_room.flywheel.lib.internal.FlwLibXplat;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.ForgeBakedModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.ForgeMultiBlockModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.MultiBlockModelBuilder;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;

public class FlwLibXplatImpl implements FlwLibXplat {
	@Override
	@UnknownNullability
	public BakedModel getBakedModel(ModelManager modelManager, ResourceLocation location) {
		return modelManager.getModel(location);
	}

	@Override
	public BakedModelBuilder createBakedModelBuilder(BakedModel bakedModel) {
		return new ForgeBakedModelBuilder(bakedModel);
	}

	@Override
	public MultiBlockModelBuilder createMultiBlockModelBuilder(BlockAndTintGetter level, Iterable<BlockPos> positions) {
		return new ForgeMultiBlockModelBuilder(level, positions);
	}
}
