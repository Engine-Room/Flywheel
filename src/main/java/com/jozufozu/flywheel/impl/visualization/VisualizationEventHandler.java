package com.jozufozu.flywheel.impl.visualization;

import com.jozufozu.flywheel.api.event.BeginFrameEvent;
import com.jozufozu.flywheel.api.event.RenderStageEvent;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.fml.LogicalSide;

public final class VisualizationEventHandler {
	private VisualizationEventHandler() {
	}

	public static void onClientTick(TickEvent.LevelTickEvent event) {
		// Make sure we don't tick on the server somehow.
		if (event.phase != TickEvent.Phase.END || event.side != LogicalSide.CLIENT) {
			return;
		}

		// The game won't be paused in the tick event, but let's make sure there's a player.
		if (Minecraft.getInstance().player == null) {
			return;
		}

		VisualizationManagerImpl manager = VisualizationManagerImpl.get(event.level);
		if (manager == null) {
			return;
		}

		manager.tick();
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

	public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
		Level level = event.getLevel();
		VisualizationManager manager = VisualizationManager.get(level);
		if (manager == null) {
			return;
		}

		manager.getEntities().queueAdd(event.getEntity());
	}

	public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
		Level level = event.getLevel();
		VisualizationManager manager = VisualizationManager.get(level);
		if (manager == null) {
			return;
		}

		manager.getEntities().queueRemove(event.getEntity());
	}
}
