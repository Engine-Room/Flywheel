package com.jozufozu.flywheel.backend.engine.batching;

import java.util.List;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;

public class CPUInstancer<I extends Instance> extends AbstractInstancer<I> {
	public CPUInstancer(InstanceType<I> type) {
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
}
