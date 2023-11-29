package com.jozufozu.flywheel.backend.engine.batching;

import java.util.ArrayList;
import java.util.List;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;

public class BatchedInstancer<I extends Instance> extends AbstractInstancer<I> {
	private final List<TransformCall<I>> transformCalls = new ArrayList<>();

	public BatchedInstancer(InstanceType<I> type) {
		super(type);
	}

	public List<I> getRange(int start, int end) {
		return instances.subList(start, end);
	}

	public List<I> getAll() {
		return instances;
	}

	public I get(int index) {
		return instances.get(index);
	}

	public void update() {
		removeDeletedInstances();
	}

	public void addTransformCall(TransformCall<I> transformCall) {
		transformCalls.add(transformCall);
	}

	public List<TransformCall<I>> getTransformCalls() {
		return transformCalls;
	}
}
