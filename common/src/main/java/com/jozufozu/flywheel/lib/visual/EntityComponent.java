package com.jozufozu.flywheel.lib.visual;

import com.jozufozu.flywheel.api.visual.DynamicVisual;

public interface EntityComponent {
	void beginFrame(DynamicVisual.Context context);

	void delete();
}
