package dev.engine_room.flywheel.impl.compat;

import org.embeddedt.embeddium.api.ChunkDataBuiltEvent;

import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;

public class EmbeddiumCompat {
	public static void init() {
		ChunkDataBuiltEvent.BUS.addListener(event -> {
			event.getDataBuilder().removeBlockEntitiesIf(VisualizationHelper::tryAddBlockEntity);
		});
	}
}
