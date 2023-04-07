package com.jozufozu.flywheel.backend.engine.batching;

import java.util.List;

import com.jozufozu.flywheel.api.struct.InstancePart;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;

public class CPUInstancer<P extends InstancePart> extends AbstractInstancer<P> {
	public CPUInstancer(StructType<P> type) {
		super(type);
	}

	public List<P> getRange(int start, int end) {
		return data.subList(start, end);
	}

	public List<P> getAll() {
		return data;
	}

	public void update() {
		removeDeletedInstances();
	}
}
