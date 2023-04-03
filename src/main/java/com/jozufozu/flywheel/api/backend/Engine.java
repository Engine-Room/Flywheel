package com.jozufozu.flywheel.api.backend;

import java.util.List;

import com.jozufozu.flywheel.api.instancer.InstancerProvider;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;

public interface Engine extends RenderDispatcher, InstancerProvider {
	void attachManagers(InstanceManager<?>... listener);

	void addDebugInfo(List<String> info);
}
