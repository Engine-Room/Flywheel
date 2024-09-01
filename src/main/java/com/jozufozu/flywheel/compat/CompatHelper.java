package com.jozufozu.flywheel.compat;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import net.fabricmc.loader.impl.util.version.VersionPredicateParser;

public class CompatHelper {
	public static final Supplier<Boolean> IS_SODIUM_LOADED = Suppliers.memoize(() -> FabricLoader.getInstance().isModLoaded("sodium"));
	public static final Supplier<Boolean> IS_EMBEDDIUM_LOADED = Suppliers.memoize(() -> FabricLoader.getInstance().isModLoaded("embeddium"));

	public static boolean IS_SODIUM_0_5 = false;
	public static boolean IS_SODIUM_0_6 = false;

	public static void initSodiumVersion() {
		if (IS_SODIUM_LOADED.get() && !IS_EMBEDDIUM_LOADED.get()) {
			try {
				VersionPredicate predicate0_5 = VersionPredicateParser.parse(">=0.5.0 <0.6.0");
				VersionPredicate predicate0_6 = VersionPredicateParser.parse("<0.6 >=0.6.0-beta.2");
				Version sodiumVersion = FabricLoader.getInstance()
						.getModContainer("sodium")
						.orElseThrow()
						.getMetadata()
						.getVersion();
				IS_SODIUM_0_5 = predicate0_5.test(sodiumVersion);
				IS_SODIUM_0_6 = predicate0_6.test(sodiumVersion);
			} catch (Throwable ignored) {}
		}
	}
}
