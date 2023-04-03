package com.jozufozu.flywheel.backend.compile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jozufozu.flywheel.api.struct.StructType;

public class CullingContextSet {
	static CullingContextSet create() {
		var builder = new CullingContextSet();
		for (StructType<?> structType : StructType.REGISTRY.getAll()) {
			builder.add(structType);
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

	private void add(StructType<?> structType) {
		var ctx = new CullingContext(structType);

		contexts.add(ctx);
	}
}
