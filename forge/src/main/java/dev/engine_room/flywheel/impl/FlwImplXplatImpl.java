package dev.engine_room.flywheel.impl;

import net.neoforged.neoforge.common.NeoForge;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.event.BeginFrameEvent;
import dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent;
import dev.engine_room.flywheel.api.event.RenderContext;
import dev.engine_room.flywheel.api.event.RenderStage;
import dev.engine_room.flywheel.api.event.RenderStageEvent;
import net.minecraft.client.multiplayer.ClientLevel;

public class FlwImplXplatImpl implements FlwImplXplat {
	@Override
	public void dispatchBeginFrameEvent(RenderContext context) {
		NeoForge.EVENT_BUS.post(new BeginFrameEvent(context));
	}

	@Override
	public void dispatchReloadLevelRendererEvent(@Nullable ClientLevel level) {
		NeoForge.EVENT_BUS.post(new ReloadLevelRendererEvent(level));
	}

	@Override
	public void dispatchRenderStageEvent(RenderContext context, RenderStage stage) {
		NeoForge.EVENT_BUS.post(new RenderStageEvent(context, stage));
	}

	@Override
	public String getVersionStr() {
		return FlywheelForge.version().toString();
	}

	@Override
	public FlwConfig getConfig() {
		return ForgeFlwConfig.INSTANCE;
	}
}
