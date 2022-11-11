package com.jozufozu.flywheel.event;

import java.util.List;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.light.LightUpdater;
import com.jozufozu.flywheel.util.WorldAttached;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public class ForgeEvents {

	public static void addToDebugScreen(List<String> right) {
		InstancedRenderDispatcher.getDebugString(right);
	}

	public static void unloadWorld(ClientLevel world) {
		WorldAttached.invalidateWorld(world);
	}

	public static void tickLight(Minecraft mc) {
		if (Backend.isGameActive())
			LightUpdater.get(mc.level).tick();
	}

}
