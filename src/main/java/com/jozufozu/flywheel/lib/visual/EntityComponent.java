package com.jozufozu.flywheel.lib.visual;

import com.jozufozu.flywheel.api.visual.VisualFrameContext;

public interface EntityComponent {
	void beginFrame(VisualFrameContext context);

	void delete();
}
