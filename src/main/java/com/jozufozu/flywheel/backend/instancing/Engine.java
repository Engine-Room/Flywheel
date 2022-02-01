package com.jozufozu.flywheel.backend.instancing;

import java.util.List;

import com.jozufozu.flywheel.api.MaterialManager;

public interface Engine extends RenderDispatcher, MaterialManager {
	void addDebugInfo(List<String> info);
}
