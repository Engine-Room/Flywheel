package com.jozufozu.flywheel.event;

import java.util.ArrayList;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.FlywheelMemory;
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

public class ForgeEvents {

	public static void addToDebugScreen(RenderGameOverlayEvent.Text event) {

		if (Minecraft.getInstance().options.renderDebug) {

			ArrayList<String> debug = event.getRight();
			debug.add("");
			debug.add("Flywheel: " + Flywheel.getVersion());

			InstancedRenderDispatcher.getDebugString(debug);

			debug.add("Memory used:");
			debug.add("GPU: " + FlywheelMemory.getGPUMemory());
			debug.add("CPU: " + FlywheelMemory.getCPUMemory());
		}
	}

	public static void unloadWorld(WorldEvent.Unload event) {
		WorldAttached.invalidateWorld(event.getWorld());
	}

	public static void tickLight(TickEvent.ClientTickEvent e) {
		if (e.phase == TickEvent.Phase.END && Backend.isGameActive()) {
			LightUpdater.get(Minecraft.getInstance().level)
					.tick();
		}
	}

}
