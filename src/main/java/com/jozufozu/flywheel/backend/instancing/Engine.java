package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.api.MaterialManager;

public interface Engine extends RenderDispatcher, MaterialManager {
	String getName();
}
