package dev.engine_room.vanillin;

import dev.engine_room.vanillin.visuals.VanillaVisuals;
import net.fabricmc.api.ClientModInitializer;

public class VanillinFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		VanillaVisuals.init();
	}
}
