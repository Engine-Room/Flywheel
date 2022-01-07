package com.jozufozu.flywheel.api;

/**
 * A marker interface custom worlds can override to indicate
 * that block entities and entities inside the world should
 * render with Flywheel.
 *
 * {@link net.minecraft.client.Minecraft#level Minecraft#level} is special cased and will support Flywheel by default.
 */
public interface FlywheelWorld {
	default boolean supportsFlywheel() {
		return true;
	}
}
