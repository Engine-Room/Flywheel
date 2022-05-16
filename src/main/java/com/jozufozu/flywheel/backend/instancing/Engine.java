package com.jozufozu.flywheel.backend.instancing;

import java.util.List;

import com.jozufozu.flywheel.api.InstancerManager;

public interface Engine extends RenderDispatcher, InstancerManager {
	void addDebugInfo(List<String> info);
}
