package com.jozufozu.flywheel.compat;

import java.util.Optional;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;

public class CompatHelper {
	public static final Supplier<Boolean> IS_SODIUM_LOADED = Suppliers.memoize(() -> FabricLoader.getInstance().isModLoaded("sodium"));
	public static final Supplier<Boolean> IS_EMBEDDIUM_LOADED = Suppliers.memoize(() -> FabricLoader.getInstance().isModLoaded("embeddium"));

	static {
		boolean is0_5Loaded = false;
		boolean is0_6Loaded = false;

		if (IS_SODIUM_LOADED.get() && !IS_EMBEDDIUM_LOADED.get()) {
			Optional<ModContainer> containerOptional = FabricLoader.getInstance()
					.getModContainer("sodium");

			if (containerOptional.isPresent()) {
				Version sodiumVersion = containerOptional.get()
						.getMetadata()
						.getVersion();

				try {
					VersionPredicate predicate0_5 = VersionPredicate.parse("~0.5.0 >=0.5.0");
					VersionPredicate predicate0_6 = VersionPredicate.parse(">=0.6.0-beta.2");
					is0_5Loaded = predicate0_5.test(sodiumVersion);
					is0_6Loaded = predicate0_6.test(sodiumVersion);
				} catch (Throwable ignored) {}
			}
		}

		IS_SODIUM_0_5 = is0_5Loaded;
		IS_SODIUM_0_6 = is0_6Loaded;
	}

	public static final boolean IS_SODIUM_0_5;
	public static final boolean IS_SODIUM_0_6;
}
