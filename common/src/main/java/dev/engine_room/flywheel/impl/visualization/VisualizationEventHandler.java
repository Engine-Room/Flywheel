package com.jozufozu.flywheel.impl.visualization;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class VisualizationEventHandler {
	private VisualizationEventHandler() {
	}

	public static void onClientTick(Minecraft minecraft, Level level) {
		// The game won't be paused in the tick event, but let's make sure there's a player.
		if (minecraft.player == null) {
			return;
		}

		VisualizationManagerImpl manager = VisualizationManagerImpl.get(level);
		if (manager == null) {
			return;
		}

		manager.tick();
	}

	public static void onBeginFrame(RenderContext context) {
		ClientLevel level = context.level();
		VisualizationManagerImpl manager = VisualizationManagerImpl.get(level);
		if (manager == null) {
			return;
		}

		manager.beginFrame(context);
	}

	public static void onRenderStage(RenderContext context, RenderStage stage) {
		ClientLevel level = context.level();
		VisualizationManagerImpl manager = VisualizationManagerImpl.get(level);
		if (manager == null) {
			return;
		}

		manager.renderStage(context, stage);
	}

	public static void onEntityJoinLevel(Level level, Entity entity) {
		VisualizationManager manager = VisualizationManager.get(level);
		if (manager == null) {
			return;
		}

		manager.getEntities().queueAdd(entity);
	}

	public static void onEntityLeaveLevel(Level level, Entity entity) {
		VisualizationManager manager = VisualizationManager.get(level);
		if (manager == null) {
			return;
		}

		manager.getEntities().queueRemove(entity);
	}
}
