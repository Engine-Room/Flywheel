package com.jozufozu.flywheel.backend.compile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jozufozu.flywheel.api.instance.InstanceType;

public class CullingContextSet {
	static CullingContextSet create() {
		var builder = new CullingContextSet();
		for (InstanceType<?> instanceType : InstanceType.REGISTRY) {
			builder.add(instanceType);
		}
		return builder;
	}

	private final List<CullingContext> contexts = new ArrayList<>();
	private final List<CullingContext> contextView = Collections.unmodifiableList(contexts);

	CullingContextSet() {
	}

	public List<CullingContext> all() {
		return contextView;
	}

	public int size() {
		return contexts.size();
	}

	private void add(InstanceType<?> instanceType) {
		var ctx = new CullingContext(instanceType);

		contexts.add(ctx);
	}
}
