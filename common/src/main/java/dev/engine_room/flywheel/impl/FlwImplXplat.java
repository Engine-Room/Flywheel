package com.jozufozu.flywheel.impl;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.internal.DependencyInjection;

import net.minecraft.client.multiplayer.ClientLevel;

public interface FlwImplXplat {
	FlwImplXplat INSTANCE = DependencyInjection.load(FlwImplXplat.class, "com.jozufozu.flywheel.impl.FlwImplXplatImpl");

	void dispatchBeginFrameEvent(RenderContext context);

	void dispatchReloadLevelRendererEvent(@Nullable ClientLevel level);

	void dispatchRenderStageEvent(RenderContext context, RenderStage stage);

	String getVersionStr();

	FlwConfig getConfig();
}
