package dev.engine_room.flywheel.impl.compat;

import dev.engine_room.flywheel.impl.FlwImplXplat;

public enum CompatMods {
	IRIS("iris"),
	SODIUM("sodium");

	public final boolean isLoaded;

	CompatMods(String modId) {
		isLoaded = FlwImplXplat.INSTANCE.getModLoaded(modId);
	}
}
