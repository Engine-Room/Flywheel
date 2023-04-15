package com.jozufozu.flywheel.handler;

import com.jozufozu.flywheel.impl.visualization.VisualizedRenderDispatcher;
import com.jozufozu.flywheel.util.FlwUtil;

import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;

public class EntityWorldHandler {
	public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
		Level level = event.getWorld();
		if (!level.isClientSide) {
			return;
		}

		if (FlwUtil.canUseVisualization(level)) {
			VisualizedRenderDispatcher.getEntities(level)
					.queueAdd(event.getEntity());
		}
	}

	public static void onEntityLeaveWorld(EntityLeaveWorldEvent event) {
		Level level = event.getWorld();
		if (!level.isClientSide) {
			return;
		}

		if (FlwUtil.canUseVisualization(level)) {
			VisualizedRenderDispatcher.getEntities(level)
					.queueRemove(event.getEntity());
		}
	}
}
