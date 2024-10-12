package dev.engine_room.flywheel.impl.compat;

import java.util.function.BooleanSupplier;

import dev.engine_room.flywheel.impl.FlwImplXplat;

public enum CompatMods {
	SODIUM("sodium"),
	IRIS("iris");

	private final BooleanSupplier isLoaded;

	CompatMods(String modid) {
		isLoaded = FlwImplXplat.INSTANCE.getModLoaded(modid);
	}

	public boolean isLoaded() {
		return isLoaded.getAsBoolean();
	}
}
