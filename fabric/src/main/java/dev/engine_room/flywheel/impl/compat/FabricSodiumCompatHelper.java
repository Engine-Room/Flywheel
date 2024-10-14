package dev.engine_room.flywheel.impl.compat;

import dev.engine_room.flywheel.impl.compat.sodium.SodiumCompatHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;

public class FabricSodiumCompatHelper {
	public static void init() {
		if (CompatMods.SODIUM.isLoaded() && !CompatMods.EMBEDDIUM.isLoaded()) {
			FabricLoader.getInstance().getModContainer("sodium").ifPresent(c -> {
				Version sodiumVersion = c.getMetadata().getVersion();

				try {
					VersionPredicate predicate0_5 = VersionPredicate.parse("~0.5.0");
					VersionPredicate predicate0_6 = VersionPredicate.parse(">=0.6.0-beta.2");
					SodiumCompatHelper.IS_SODIUM_0_5 = predicate0_5.test(sodiumVersion);
					SodiumCompatHelper.IS_SODIUM_0_6 = predicate0_6.test(sodiumVersion);
				} catch (VersionParsingException ignored) {}
			});
		}
	}
}
