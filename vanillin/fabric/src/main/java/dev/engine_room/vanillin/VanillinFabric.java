package dev.engine_room.vanillin;

import dev.engine_room.flywheel.api.event.ReloadLevelRendererCallback;
import dev.engine_room.vanillin.item.SodiumAnimatedTextureCompat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.server.packs.PackType;

public class VanillinFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		VanillaVisuals.init();
		FabricVanillinConfig.INSTANCE.load();
		FabricVanillinConfig.INSTANCE.apply(VanillaVisuals.CONFIGURATOR);
		FabricVanillinConfig.INSTANCE.save();

		ReloadLevelRendererCallback.EVENT.register(level -> SodiumAnimatedTextureCompat.onReloadRenderer());
		LevelRenderEvents.START_MAIN.register(context -> SodiumAnimatedTextureCompat.beginFrame());

		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(VanillinClientResourceReloadListener.ID, VanillinClientResourceReloadListener.INSTANCE);
	}
}
