package com.jozufozu.flywheel.impl.visualization;

import com.jozufozu.flywheel.api.event.BeginFrameEvent;
import com.jozufozu.flywheel.api.event.RenderStageEvent;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.jozufozu.flywheel.lib.util.FlwUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;

public final class VisualizationEventHandler {
	private VisualizationEventHandler() {
	}

	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END || !FlwUtil.isGameActive()) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		if (mc.isPaused()) {
			return;
		}

		Entity cameraEntity = mc.getCameraEntity() == null ? mc.player : mc.getCameraEntity();
		if (cameraEntity == null) {
			return;
		}

		Level level = cameraEntity.level;
		VisualizationManagerImpl manager = VisualizationManagerImpl.get(level);
		if (manager == null) {
			return;
		}

		double cameraX = cameraEntity.getX();
		double cameraY = cameraEntity.getEyeY();
		double cameraZ = cameraEntity.getZ();

		manager.tick(cameraX, cameraY, cameraZ);
	}

	public static void onBeginFrame(BeginFrameEvent event) {
		ClientLevel level = event.getContext().level();
		VisualizationManagerImpl manager = VisualizationManagerImpl.get(level);
		if (manager == null) {
			return;
		}

		manager.beginFrame(event.getContext());
	}

	public static void onRenderStage(RenderStageEvent event) {
		ClientLevel level = event.getContext().level();
		VisualizationManagerImpl manager = VisualizationManagerImpl.get(level);
		if (manager == null) {
			return;
		}

		manager.renderStage(event.getContext(), event.getStage());
	}

	public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
		Level level = event.getWorld();
		VisualizationManager manager = VisualizationManager.get(level);
		if (manager == null) {
			return;
		}

		manager.getEntities().queueAdd(event.getEntity());
	}

	public static void onEntityLeaveWorld(EntityLeaveWorldEvent event) {
		Level level = event.getWorld();
		VisualizationManager manager = VisualizationManager.get(level);
		if (manager == null) {
			return;
		}

		manager.getEntities().queueRemove(event.getEntity());
	}
}
