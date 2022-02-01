package com.jozufozu.flywheel.event;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.light.LightUpdater;
import com.jozufozu.flywheel.util.WorldAttached;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ForgeEvents {

	@SubscribeEvent
	public static void addToDebugScreen(RenderGameOverlayEvent.Text event) {

		if (Minecraft.getInstance().options.renderDebug) {

			InstancedRenderDispatcher.getDebugString(event.getRight());
		}
	}

	@SubscribeEvent
	public static void unloadWorld(WorldEvent.Unload event) {
		WorldAttached.invalidateWorld(event.getWorld());
	}

	@SubscribeEvent
	public static void tickLight(TickEvent.ClientTickEvent e) {
		if (e.phase == TickEvent.Phase.END && Backend.isGameActive())
			LightUpdater.get(Minecraft.getInstance().level).tick();
	}

}
