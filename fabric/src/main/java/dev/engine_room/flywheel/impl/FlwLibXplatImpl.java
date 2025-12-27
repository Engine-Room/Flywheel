package dev.engine_room.flywheel.impl;

import dev.engine_room.flywheel.lib.model.baked.BlockModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.LevelModelBuilder;

import dev.engine_room.flywheel.lib.internal.FlwLibXplat;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import dev.engine_room.flywheel.lib.model.baked.ModelBuilderImpl;

public class FlwLibXplatImpl implements FlwLibXplat {
	@Override
	public SimpleModel buildBlockModelBuilder(BlockModelBuilder builder) {
		return ModelBuilderImpl.buildBlockModelBuilder(builder);
	}

	@Override
	public SimpleModel buildLevelModelBuilder(LevelModelBuilder builder) {
		return ModelBuilderImpl.buildLevelModelBuilder(builder);
	}
}
