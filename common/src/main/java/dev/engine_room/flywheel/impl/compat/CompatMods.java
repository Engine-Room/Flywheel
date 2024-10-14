package dev.engine_room.flywheel.impl.compat;

import dev.engine_room.flywheel.impl.FlwImplXplat;

public enum CompatMods {
	SODIUM("sodium"),
	IRIS("iris");

	public final boolean isLoaded;

	CompatMods(String modId) {
		isLoaded = FlwImplXplat.INSTANCE.getModLoaded(modId);
	}
}
