package com.jozufozu.flywheel.api;

/**
 * A marker interface custom levels can override to indicate
 * that block entities and entities inside the level should
 * render with Flywheel.
 *
 * {@link net.minecraft.client.Minecraft#level Minecraft#level} is special cased and will support Flywheel by default.
 */
public interface FlywheelLevel {
	default boolean supportsFlywheel() {
		return true;
	}
}
