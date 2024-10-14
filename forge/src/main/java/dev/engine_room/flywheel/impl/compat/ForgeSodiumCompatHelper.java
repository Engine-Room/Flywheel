package dev.engine_room.flywheel.impl.compat;

import dev.engine_room.flywheel.impl.compat.sodium.SodiumCompatHelper;

public class ForgeSodiumCompatHelper {
	public static void init() {
		if (CompatMods.SODIUM.isLoaded() && !(CompatMods.RUBIDIUM.isLoaded() && CompatMods.EMBEDDIUM.isLoaded())) {
			SodiumCompatHelper.IS_SODIUM_0_6 = true;
		}
	}
}
