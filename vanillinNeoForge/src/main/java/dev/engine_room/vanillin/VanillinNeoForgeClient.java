package dev.engine_room.vanillin;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;

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
	}
}
