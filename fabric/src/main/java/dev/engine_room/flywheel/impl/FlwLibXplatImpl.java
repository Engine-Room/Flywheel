package dev.engine_room.flywheel.impl;

import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.UnknownNullability;

import dev.engine_room.flywheel.lib.internal.FlwLibXplat;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.BlockModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.ModelBuilderImpl;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;

public class FlwLibXplatImpl implements FlwLibXplat {
	@Override
	@UnknownNullability
	public BakedModel getBakedModel(ModelManager modelManager, Identifier id) {
		return modelManager.getModel(id);
	}

	@Override
	public SimpleModel buildBakedModelBuilder(BakedModelBuilder builder) {
		return ModelBuilderImpl.buildBakedModelBuilder(builder);
	}

	@Override
	public SimpleModel buildBlockModelBuilder(BlockModelBuilder builder) {
		return ModelBuilderImpl.buildBlockModelBuilder(builder);
	}
}
