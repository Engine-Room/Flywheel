package dev.engine_room.vanillin;

import net.fabricmc.api.ClientModInitializer;

public class VanillinFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		VanillaVisuals.init();
		FabricVanillinConfig.INSTANCE.load();
		FabricVanillinConfig.INSTANCE.apply(VanillaVisuals.CONFIGURATOR);
		FabricVanillinConfig.INSTANCE.save();
	}
}
