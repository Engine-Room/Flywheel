package dev.engine_room.flywheel.impl;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.event.BeginFrameEvent;
import dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent;
import dev.engine_room.flywheel.api.event.RenderContext;
import dev.engine_room.flywheel.api.event.RenderStage;
import dev.engine_room.flywheel.api.event.RenderStageEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.common.MinecraftForge;

public class FlwImplXplatImpl implements FlwImplXplat {
	@Override
	public void dispatchBeginFrameEvent(RenderContext context) {
		MinecraftForge.EVENT_BUS.post(new BeginFrameEvent(context));
	}

	@Override
	public void dispatchReloadLevelRendererEvent(@Nullable ClientLevel level) {
		MinecraftForge.EVENT_BUS.post(new ReloadLevelRendererEvent(level));
	}

	@Override
	public void dispatchRenderStageEvent(RenderContext context, RenderStage stage) {
		MinecraftForge.EVENT_BUS.post(new RenderStageEvent(context, stage));
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
