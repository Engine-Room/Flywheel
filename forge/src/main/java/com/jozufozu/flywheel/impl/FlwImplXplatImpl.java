package com.jozufozu.flywheel.impl;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.event.BeginFrameEvent;
import com.jozufozu.flywheel.api.event.ReloadLevelRendererEvent;
import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.event.RenderStageEvent;

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
