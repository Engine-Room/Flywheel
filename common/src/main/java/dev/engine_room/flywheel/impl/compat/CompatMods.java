package dev.engine_room.flywheel.impl.compat;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import net.fabricmc.loader.api.FabricLoader;

public enum CompatMods {
	SODIUM,
	EMBEDDIUM;

	private final Supplier<Boolean> isLoaded;

	CompatMods() {
		isLoaded = Suppliers.memoize(() -> FabricLoader.getInstance().isModLoaded(name()));
	}

	public boolean isLoaded() {
		return isLoaded.get();
	}
}
