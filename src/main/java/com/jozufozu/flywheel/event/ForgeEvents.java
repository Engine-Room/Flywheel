package com.jozufozu.flywheel.event;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.light.LightUpdater;
import com.jozufozu.flywheel.util.WorldAttached;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ForgeEvents {

	@SubscribeEvent
	public static void addToDebugScreen(CustomizeGuiOverlayEvent.DebugText event) {
		if (Minecraft.getInstance().options.renderDebug) {
			InstancedRenderDispatcher.getDebugString(event.getRight());
		}
	}

	@SubscribeEvent
	public static void unloadWorld(LevelEvent.Unload event) {
		WorldAttached.invalidateWorld(event.getLevel());
	}

	@SubscribeEvent
	public static void tickLight(TickEvent.ClientTickEvent e) {
		if (e.phase == TickEvent.Phase.END && Backend.isGameActive())
			LightUpdater.get(Minecraft.getInstance().level).tick();
	}

}
