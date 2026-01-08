package dev.engine_room.flywheel.lib.internal;

import dev.engine_room.flywheel.api.internal.DependencyInjection;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import dev.engine_room.flywheel.lib.model.baked.BlockModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.LevelModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.Identifier;

public interface FlwLibXplat {
	FlwLibXplat INSTANCE = DependencyInjection.load(FlwLibXplat.class, "dev.engine_room.flywheel.impl.FlwLibXplatImpl");

	PartialModel createPartialModel(Identifier modelId);

	SimpleModel buildBlockModelBuilder(BlockModelBuilder builder);

	SimpleModel buildLevelModelBuilder(LevelModelBuilder builder);
}
