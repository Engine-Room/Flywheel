package dev.engine_room.flywheel.lib.model.baked;

import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;

@ApiStatus.Internal
public final class PartialModelEventHandler {
	private PartialModelEventHandler() {
	}

	public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
		for (ResourceLocation modelLocation : PartialModel.ALL.keySet()) {
			event.register(modelLocation);
		}
	}

	public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
		PartialModel.populateOnInit = true;
		Map<ModelResourceLocation, BakedModel> models = event.getModels();

		for (PartialModel partial : PartialModel.ALL.values()) {
			partial.bakedModel = models.get(partial.modelLocation());
		}
	}
}
