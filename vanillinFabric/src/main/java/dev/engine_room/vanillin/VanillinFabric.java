package dev.engine_room.vanillin;

import dev.engine_room.flywheel.api.event.ReloadLevelRendererCallback;
import dev.engine_room.vanillin.item.SodiumAnimatedTextureCompat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class VanillinFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		VanillaVisuals.init();
		FabricVanillinConfig.INSTANCE.load();
		FabricVanillinConfig.INSTANCE.apply(VanillaVisuals.CONFIGURATOR);
		FabricVanillinConfig.INSTANCE.save();

		ReloadLevelRendererCallback.EVENT.register(level -> SodiumAnimatedTextureCompat.onReloadRenderer());
		WorldRenderEvents.START.register(context -> SodiumAnimatedTextureCompat.beginFrame());
	}
}
