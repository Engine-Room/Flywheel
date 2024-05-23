package dev.engine_room.flywheel.impl;

import java.util.List;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.memory.FlwMemoryTracker;
import dev.engine_room.flywheel.lib.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Vec3i;

public final class FlwDebugInfo {
	private FlwDebugInfo() {
	}

	public static void addDebugInfo(Minecraft minecraft, List<String> systemInfo) {
		if (minecraft.showOnlyReducedInfo()) {
			return;
		}

		systemInfo.add("");
		systemInfo.add("Flywheel: " + FlwImplXplat.INSTANCE.getVersionStr());
		systemInfo.add("Backend: " + BackendManagerImpl.getBackendString());
		systemInfo.add("Update limiting: " + (FlwConfig.INSTANCE.limitUpdates() ? "on" : "off"));

		VisualizationManager manager = VisualizationManager.get(minecraft.level);
		if (manager != null) {
			systemInfo.add("B: " + manager.getBlockEntities().getVisualCount()
					+ ", E: " + manager.getEntities().getVisualCount()
					+ ", F: " + manager.getEffects().getVisualCount());
			Vec3i renderOrigin = manager.getRenderOrigin();
			systemInfo.add("Origin: " + renderOrigin.getX() + ", " + renderOrigin.getY() + ", " + renderOrigin.getZ());
		}

		systemInfo.add("Memory Usage: CPU: " + StringUtil.formatBytes(FlwMemoryTracker.getCPUMemory()) + ", GPU: " + StringUtil.formatBytes(FlwMemoryTracker.getGPUMemory()));
	}
}
