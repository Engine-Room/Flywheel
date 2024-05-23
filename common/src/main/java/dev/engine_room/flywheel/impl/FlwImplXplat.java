package dev.engine_room.flywheel.impl;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.event.RenderContext;
import dev.engine_room.flywheel.api.event.RenderStage;
import dev.engine_room.flywheel.api.internal.DependencyInjection;
import net.minecraft.client.multiplayer.ClientLevel;

public interface FlwImplXplat {
	FlwImplXplat INSTANCE = DependencyInjection.load(FlwImplXplat.class, "dev.engine_room.flywheel.impl.FlwImplXplatImpl");

	void dispatchBeginFrameEvent(RenderContext context);

	void dispatchReloadLevelRendererEvent(@Nullable ClientLevel level);

	void dispatchRenderStageEvent(RenderContext context, RenderStage stage);

	String getVersionStr();

	FlwConfig getConfig();
}
