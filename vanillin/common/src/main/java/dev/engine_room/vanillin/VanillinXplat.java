package dev.engine_room.vanillin;

import dev.engine_room.flywheel.api.internal.DependencyInjection;

public interface VanillinXplat {
	VanillinXplat INSTANCE = DependencyInjection.load(VanillinXplat.class, "dev.engine_room.vanillin.VanillinXplatImpl");

	boolean isDevelopmentEnvironment();

	boolean isModLoaded(String modId);
}
