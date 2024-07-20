package dev.engine_room.flywheel.lib.visual.component;

import dev.engine_room.flywheel.api.visual.DynamicVisual;

public interface EntityComponent {
	void beginFrame(DynamicVisual.Context context);

	void delete();
}
