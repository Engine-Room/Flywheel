package dev.engine_room.flywheel.impl.compat;

import dev.engine_room.flywheel.impl.FlwImplXplat;

public enum CompatMods {
	EMBEDDIUM("embeddium"),
	IRIS("iris"),
	OCULUS("oculus"),
	SODIUM("sodium");

	public final boolean isLoaded;

	CompatMods(String modId) {
		isLoaded = FlwImplXplat.INSTANCE.getModLoaded(modId);
	}
}
