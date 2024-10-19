package dev.engine_room.flywheel.impl.compat;

import java.util.Optional;

import dev.engine_room.flywheel.impl.FlwImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;

public final class FabricSodiumCompat {
	public static final boolean USE_0_5_COMPAT;
	public static final boolean USE_0_6_COMPAT;

	static {
		boolean use0_5Compat = false;
		boolean use0_6Compat = false;

		Optional<ModContainer> optionalModContainer = FabricLoader.getInstance().getModContainer(CompatMod.SODIUM.id);

		if (optionalModContainer.isPresent()) {
			ModContainer modContainer = optionalModContainer.get();
			Version sodiumVersion = modContainer.getMetadata().getVersion();

			try {
				VersionPredicate predicate0_5 = VersionPredicate.parse("~0.5.0");
				VersionPredicate predicate0_6 = VersionPredicate.parse(">=0.6.0-beta.2");
				use0_5Compat = predicate0_5.test(sodiumVersion);
				use0_6Compat = predicate0_6.test(sodiumVersion);
			} catch (VersionParsingException e) {
				FlwImpl.LOGGER.debug("Failed to parse Sodium version predicates", e);
			}
		}

		USE_0_5_COMPAT = use0_5Compat;
		USE_0_6_COMPAT = use0_6Compat;
	}

	static {
		if (USE_0_5_COMPAT) {
			FlwImpl.LOGGER.debug("Detected Sodium 0.5");
		}
	}

	private FabricSodiumCompat() {
	}
}
