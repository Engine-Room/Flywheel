package com.jozufozu.flywheel.fabric.event;

import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.event.GatherContextEvent;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.jozufozu.flywheel.fabric.event.EventContext.Listener;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.ResourceLocation;

public final class FlywheelEvents {
	public static final Event<Listener<BeginFrameEvent>> BEGIN_FRAME = createSimple();
	public static final Event<Listener<GatherContextEvent>> GATHER_CONTEXT = createSimple();
	public static final Event<Listener<ReloadRenderersEvent>> RELOAD_RENDERERS = createSimple();
	public static final Event<Listener<RenderLayerEvent>> RENDER_LAYER = createSimple();

	public static final ResourceLocation PRE_STAGE = new ResourceLocation("flywheel:pre_stage");
	public static final ResourceLocation MAIN_STAGE = new ResourceLocation("flywheel:main_stage");

	private static <C extends EventContext> Event<Listener<C>> createSimple() {
		return EventFactory.createWithPhases(Listener.class,
			listeners -> context -> {
				for (Listener<C> listener : listeners) {
					listener.handleEvent(context);

					if (context.isCanceled()) {
						return;
					}
				}
			}, PRE_STAGE, MAIN_STAGE
		);
	}
}
