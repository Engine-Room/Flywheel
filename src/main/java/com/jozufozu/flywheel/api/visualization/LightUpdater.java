package com.jozufozu.flywheel.api.visualization;

import com.jozufozu.flywheel.lib.light.LightListener;

public interface LightUpdater {
	void addListener(LightListener listener);

	void removeListener(LightListener listener);
}
