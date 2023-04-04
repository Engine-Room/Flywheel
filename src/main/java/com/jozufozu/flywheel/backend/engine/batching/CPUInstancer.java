package com.jozufozu.flywheel.backend.engine.batching;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;

public class CPUInstancer<D extends InstancedPart> extends AbstractInstancer<D> {

	public CPUInstancer(StructType<D> type) {
		super(type);
	}

	void update() {
		if (anyToRemove) {
			data.removeIf(InstancedPart::isRemoved);
			anyToRemove = false;
		}
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
