package dev.engine_room.vanillin;

import dev.engine_room.vanillin.visuals.VanillaVisuals;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Vanillin.ID, dist = Dist.CLIENT)
public class VanillinNeoForgeClient {
	public VanillinNeoForgeClient(IEventBus modEventBus) {
		IEventBus neoEventBus = NeoForge.EVENT_BUS;
		VanillaVisuals.init();
	}
}
