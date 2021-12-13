package com.jozufozu.flywheel.event;

import java.util.List;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.light.LightUpdater;
import com.jozufozu.flywheel.util.WorldAttached;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public class ForgeEvents {

	public static void addToDebugScreen(List<String> right) {

		String text = "Flywheel: " + Backend.getInstance()
				.getBackendDescriptor();
		if (right.size() < 10) {
			right.add("");
			right.add(text);
		} else {
			right.add(9, "");
			right.add(10, text);
		}
	}

	public static void unloadWorld(ClientLevel world) {
		WorldAttached.invalidateWorld(world);
	}

	public static void tickLight(Minecraft mc) {
		if (Backend.isGameActive())
			LightUpdater.get(mc.level).tick();
	}

}
