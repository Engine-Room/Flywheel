package com.jozufozu.flywheel.backend.instancing;

import java.util.List;

import com.jozufozu.flywheel.api.instancer.InstancerManager;

public interface Engine extends RenderDispatcher, InstancerManager {
	void attachManagers(InstanceManager<?>... listener);

	void addDebugInfo(List<String> info);
}
