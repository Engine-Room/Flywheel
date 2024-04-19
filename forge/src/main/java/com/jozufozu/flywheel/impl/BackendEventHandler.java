package com.jozufozu.flywheel.impl;

import com.jozufozu.flywheel.api.event.EndClientResourceReloadEvent;
import com.jozufozu.flywheel.api.event.ReloadLevelRendererEvent;
import com.jozufozu.flywheel.impl.visualization.VisualizationManagerImpl;

import net.minecraft.client.multiplayer.ClientLevel;

public class BackendEventHandler {
	public static void onEndClientResourceReload(EndClientResourceReloadEvent event) {
		if (event.error()
				.isPresent()) {
			return;
		}

		BackendManagerImpl.chooseBackend();
		VisualizationManagerImpl.resetAll();
	}

	public static void onReloadLevelRenderer(ReloadLevelRendererEvent event) {
		BackendManagerImpl.chooseBackend();

		ClientLevel level = event.level();
		if (level != null) {
			VisualizationManagerImpl.reset(level);
		}
	}
}
