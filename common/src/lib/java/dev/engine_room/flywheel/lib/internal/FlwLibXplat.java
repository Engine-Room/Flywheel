package dev.engine_room.flywheel.lib.internal;

import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.UnknownNullability;

import dev.engine_room.flywheel.api.internal.DependencyInjection;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.BlockModelBuilder;
import net.minecraft.client.resources.model.ModelManager;

public interface FlwLibXplat {
	FlwLibXplat INSTANCE = DependencyInjection.load(FlwLibXplat.class, "dev.engine_room.flywheel.impl.FlwLibXplatImpl");

	@UnknownNullability
	BakedModel getBakedModel(ModelManager modelManager, Identifier location);

	SimpleModel buildBakedModelBuilder(BakedModelBuilder builder);

	SimpleModel buildBlockModelBuilder(BlockModelBuilder builder);
}
