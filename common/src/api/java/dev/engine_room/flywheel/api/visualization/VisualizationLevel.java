package dev.engine_room.flywheel.api.visualization;

import net.minecraft.world.level.LevelAccessor;

/**
 * A marker interface custom levels can override to indicate
 * that block entities and entities inside the level should
 * render with Flywheel.
 * <br>
 * {@link net.minecraft.client.Minecraft#level Minecraft#level} is special cased and will support Flywheel by default.
 */
public interface VisualizationLevel extends LevelAccessor {
	default boolean supportsVisualization() {
		return true;
	}
}
