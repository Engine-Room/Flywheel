package dev.engine_room.flywheel.impl;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.event.BeginFrameCallback;
import dev.engine_room.flywheel.api.event.ReloadLevelRendererCallback;
import dev.engine_room.flywheel.api.event.RenderContext;
import dev.engine_room.flywheel.api.event.RenderStage;
import dev.engine_room.flywheel.api.event.RenderStageCallback;
import net.minecraft.client.multiplayer.ClientLevel;

public class FlwImplXplatImpl implements FlwImplXplat {
	@Override
	public void dispatchBeginFrameEvent(RenderContext context) {
		BeginFrameCallback.EVENT.invoker().onBeginFrame(context);
	}

	@Override
	public void dispatchReloadLevelRendererEvent(@Nullable ClientLevel level) {
		ReloadLevelRendererCallback.EVENT.invoker().onReloadLevelRenderer(level);
	}

	@Override
	public void dispatchRenderStageEvent(RenderContext context, RenderStage stage) {
		RenderStageCallback.EVENT.invoker().onRenderStage(context, stage);
	}

	@Override
	public String getVersionStr() {
		return FlywheelFabric.version().getFriendlyString();
	}

	@Override
	public FlwConfig getConfig() {
		return FabricFlwConfig.INSTANCE;
	}
}
