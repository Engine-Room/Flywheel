package com.jozufozu.flywheel.api.instance.effect;

import java.util.Collection;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instancer.InstancerProvider;

public interface Effect {
	Collection<Instance> createInstances(InstancerProvider instancerManager);
}
