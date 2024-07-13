package com.jozufozu.flywheel.compat;

import org.embeddedt.embeddium.api.ChunkDataBuiltEvent;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;

public class EmbeddiumCompat {
	public static void init() {
		ChunkDataBuiltEvent.BUS.addListener(event -> {
			event.getDataBuilder().removeBlockEntitiesIf(InstancedRenderDispatcher::tryAddBlockEntity);
		});
	}
}
