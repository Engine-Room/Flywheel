package com.jozufozu.flywheel.event;

import java.util.ArrayList;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.backend.memory.FlwMemoryTracker;
import com.jozufozu.flywheel.light.LightUpdater;
import com.jozufozu.flywheel.util.StringUtil;
import com.jozufozu.flywheel.util.WorldAttached;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;

public class ForgeEvents {

	public static void addToDebugScreen(RenderGameOverlayEvent.Text event) {
		if (Minecraft.getInstance().options.renderDebug) {
			ArrayList<String> debug = event.getRight();
			debug.add("");
			debug.add("Flywheel: " + Flywheel.getVersion());

			InstancedRenderDispatcher.getDebugString(debug);

			debug.add("Memory Usage: CPU: " + StringUtil.formatBytes(FlwMemoryTracker.getCPUMemory()) + ", GPU: " + StringUtil.formatBytes(FlwMemoryTracker.getGPUMemory()));
		}
	}

	public static void unloadWorld(WorldEvent.Unload event) {
		WorldAttached.invalidateWorld(event.getWorld());
	}

	public static void tickLight(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END && Backend.isGameActive()) {
			LightUpdater.get(Minecraft.getInstance().level)
					.tick();
		}
	}

}
