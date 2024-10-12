package dev.engine_room.flywheel.impl.compat;

import org.embeddedt.embeddium.api.ChunkDataBuiltEvent;

public class EmbeddiumCompat {
	public static void init() {
		ChunkDataBuiltEvent.BUS.addListener(event -> {
			event.getDataBuilder().removeBlockEntitiesIf(InstancedRenderDispatcher::tryAddBlockEntity);
		});
	}
}
