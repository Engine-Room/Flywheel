package dev.engine_room.vanillin;

import dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent;
import dev.engine_room.vanillin.item.SodiumAnimatedTextureCompat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Vanillin.ID, dist = Dist.CLIENT)
public class VanillinNeoForgeClient {
	public VanillinNeoForgeClient() {
		var modLoadingContext = ModLoadingContext.get();

		IEventBus modEventBus = modLoadingContext.getActiveContainer()
				.getEventBus();

		VanillaVisuals.init();
		NeoForgeVanillinConfig.INSTANCE.registerSpecs(modLoadingContext);

		modEventBus.<ModConfigEvent>addListener(event -> {
			if (event.getConfig()
					.getModId()
					.equals(Vanillin.ID)) {
				NeoForgeVanillinConfig.INSTANCE.apply();
			}
		});

		NeoForge.EVENT_BUS.addListener((ReloadLevelRendererEvent e) -> SodiumAnimatedTextureCompat.onReloadRenderer());

		NeoForge.EVENT_BUS.addListener((RenderFrameEvent.Pre stage) -> SodiumAnimatedTextureCompat.beginFrame());

	}
}
