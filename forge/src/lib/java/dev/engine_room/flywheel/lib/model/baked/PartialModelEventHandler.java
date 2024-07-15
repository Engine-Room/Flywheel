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
		for (PartialModel partial : PartialModel.ALL) {
			event.register(ModelResourceLocation.standalone(partial.getLocation()));
		}

		PartialModel.tooLate = true;
	}

	public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
		Map<ModelResourceLocation, BakedModel> models = event.getModels();

		for (PartialModel partial : PartialModel.ALL) {
			partial.set(models.get(ModelResourceLocation.standalone(partial.getLocation())));
		}
	}
}
