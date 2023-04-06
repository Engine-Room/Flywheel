package com.jozufozu.flywheel.backend.engine.batching;

import com.jozufozu.flywheel.api.instancer.InstancePart;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;

public class CPUInstancer<P extends InstancePart> extends AbstractInstancer<P> {

	public CPUInstancer(StructType<P> type) {
		super(type);
	}

	void update() {
		if (!deleted.isEmpty()) {
			removeDeletedInstances();
		}
	}
}
