package com.jozufozu.flywheel.backend.instancing.effect;

import java.util.Collection;

import com.jozufozu.flywheel.api.InstancerManager;
import com.jozufozu.flywheel.backend.instancing.AbstractInstance;

public interface Effect {

	Collection<? extends AbstractInstance> createInstances(InstancerManager instancerManager);
}
