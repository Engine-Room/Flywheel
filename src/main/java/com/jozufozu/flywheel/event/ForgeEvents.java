package com.jozufozu.flywheel.event;

import java.util.List;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;

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

	public static void onLoadWorld(ClientLevel world) {
		if (Backend.isFlywheelWorld(world)) {
			InstancedRenderDispatcher.loadAllInWorld(world);
		}
	}

}
