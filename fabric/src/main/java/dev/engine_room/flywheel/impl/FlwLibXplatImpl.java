package dev.engine_room.flywheel.impl;

import dev.engine_room.flywheel.lib.internal.FlwLibXplat;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import dev.engine_room.flywheel.lib.model.baked.BlockModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.FabricPartialModel;
import dev.engine_room.flywheel.lib.model.baked.LevelModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.ModelBuilderImpl;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.Identifier;

public class FlwLibXplatImpl implements FlwLibXplat {
	@Override
	public PartialModel createPartialModel(Identifier modelId) {
		return FabricPartialModel.of(modelId);
	}

	@Override
	public SimpleModel buildBlockModelBuilder(BlockModelBuilder builder) {
		return ModelBuilderImpl.buildBlockModelBuilder(builder);
	}

	@Override
	public SimpleModel buildLevelModelBuilder(LevelModelBuilder builder) {
		return ModelBuilderImpl.buildLevelModelBuilder(builder);
	}
}
