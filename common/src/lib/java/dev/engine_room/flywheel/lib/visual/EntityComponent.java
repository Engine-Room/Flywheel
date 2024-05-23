package dev.engine_room.flywheel.lib.visual;

import dev.engine_room.flywheel.api.visual.DynamicVisual;

public interface EntityComponent {
	void beginFrame(DynamicVisual.Context context);

	void delete();
}
