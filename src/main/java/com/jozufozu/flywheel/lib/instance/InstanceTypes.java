package com.jozufozu.flywheel.lib.instance;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.api.instance.InstanceType;

public final class InstanceTypes {
	public static final InstanceType<TransformedInstance> TRANSFORMED = InstanceType.REGISTRY.registerAndGet(new TransformedType());
	public static final InstanceType<OrientedInstance> ORIENTED = InstanceType.REGISTRY.registerAndGet(new OrientedType());

	private InstanceTypes() {
	}

	@ApiStatus.Internal
	public static void init() {
	}
}
