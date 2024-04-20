package com.jozufozu.flywheel.lib.model.baked;

import net.minecraftforge.client.event.ModelEvent;

public class PartialModelEventHandler {
	public static void onModelRegistry(ModelEvent.RegisterAdditional event) {
		for (PartialModel partial : PartialModel.ALL) {
			event.register(partial.getLocation());
		}

		PartialModel.tooLate = true;
	}

	public static void onModelBake(ModelEvent.BakingCompleted event) {
		var modelRegistry = event.getModels();
		for (PartialModel partial : PartialModel.ALL) {
			partial.set(modelRegistry.get(partial.getLocation()));
		}
	}
}
