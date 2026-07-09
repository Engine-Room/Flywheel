package dev.engine_room.flywheel.lib.model.baked;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

@ApiStatus.Internal
public final class PartialModelEventHandler {
	private static final Map<Identifier, StandaloneModelKey<BlockStateModel>> MODEL_KEYS = new ConcurrentHashMap<>();

	private PartialModelEventHandler() {
	}

	public static StandaloneModelKey<BlockStateModel> getKey(Identifier modelLocation) {
		return MODEL_KEYS.computeIfAbsent(modelLocation, PartialModelEventHandler::createKey);
	}

	private static StandaloneModelKey<BlockStateModel> createKey(Identifier modelLocation) {
		ModelDebugName debugName = () -> "Flywheel partial model " + modelLocation;
		return new StandaloneModelKey<>(debugName);
	}

	public static void onRegisterStandalone(ModelEvent.RegisterStandalone event) {
		for (Identifier modelLocation : PartialModel.ALL.keySet()) {
			event.register(getKey(modelLocation), SimpleUnbakedStandaloneModel.blockStateModel(modelLocation));
		}
	}

	public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
		PartialModel.populateOnInit = true;
		ModelManager modelManager = event.getModelManager();

		for (PartialModel partial : PartialModel.ALL.values()) {
			partial.bakedModel = modelManager.getStandaloneModel(getKey(partial.modelLocation()));
		}
	}
}
