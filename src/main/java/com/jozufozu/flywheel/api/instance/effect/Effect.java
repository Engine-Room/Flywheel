package com.jozufozu.flywheel.api.instance.effect;

import java.util.Collection;

import com.jozufozu.flywheel.api.instance.EffectInstance;
import com.jozufozu.flywheel.api.instance.controller.InstanceContext;

// TODO: add level getter?
// TODO: return single instance instead of many?
public interface Effect {
	Collection<EffectInstance<?>> createInstances(InstanceContext ctx);
}
