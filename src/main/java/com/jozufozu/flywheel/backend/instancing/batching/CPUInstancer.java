package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.List;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.instancing.AbstractInstancer;

public class CPUInstancer<D extends InstancedPart> extends AbstractInstancer<D> {

	public CPUInstancer(StructType<D> type) {
		super(type);
	}

	void setup() {
		if (anyToRemove) {
			data.removeIf(InstancedPart::isRemoved);
			anyToRemove = false;
		}
	}

	public List<D> getRange(int start, int end) {
		return data.subList(start, end);
	}

	public List<D> getAll() {
		return data;
	}

	@Override
	public void notifyDirty() {
		// noop
	}

	@Override
	public void delete() {
		// noop
	}
}
