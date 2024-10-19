package dev.engine_room.flywheel.impl.compat;

import org.embeddedt.embeddium.api.ChunkDataBuiltEvent;

import dev.engine_room.flywheel.impl.FlwImpl;
import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;

public final class EmbeddiumCompat {
	public static final boolean ACTIVE = CompatMod.EMBEDDIUM.isLoaded;

	static {
		if (ACTIVE) {
			FlwImpl.LOGGER.debug("Detected Embeddium");
		}
	}

	private EmbeddiumCompat() {
	}

	public static void init() {
		if (ACTIVE) {
			Internals.init();
		}
	}

	private static final class Internals {
		static void init() {
			ChunkDataBuiltEvent.BUS.addListener(event -> {
				event.getDataBuilder().removeBlockEntitiesIf(VisualizationHelper::tryAddBlockEntity);
			});
		}
	}
}
