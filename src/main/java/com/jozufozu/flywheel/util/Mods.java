package com.jozufozu.flywheel.util;

import java.util.Optional;
import java.util.function.Supplier;

import net.fabricmc.loader.api.FabricLoader;

/**
 * For compatibility with and without another mod present, we have to define load conditions of the specific code
 */
public enum Mods {
	STARLIGHT("starlight");

	private final String id;

	Mods(String id) {
		this.id = id;
	}

	/**
	 * @return a boolean of whether the mod is loaded or not based on mod id
	 */
	public boolean isLoaded() {
		return FabricLoader.getInstance().isModLoaded(id);
	}

	/**
	 * Simple hook to run code if a mod is installed
	 * @param toRun will be run only if the mod is loaded
	 * @return Optional.empty() if the mod is not loaded, otherwise an Optional of the return value of the given supplier
	 */
	public <T> Optional<T> runIfInstalled(Supplier<Supplier<T>> toRun) {
		if (isLoaded())
			return Optional.of(toRun.get().get());
		return Optional.empty();
	}

	/**
	 * Simple hook to execute code if a mod is installed
	 * @param toExecute will be executed only if the mod is loaded
	 */
	public void executeIfInstalled(Supplier<Runnable> toExecute) {
		if (isLoaded()) {
			toExecute.get().run();
		}
	}
}
