package com.jozufozu.flywheel.backend.instancing.indirect;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.instancing.AbstractInstancer;
import com.jozufozu.flywheel.core.layout.BufferLayout;

public class IndirectInstancer<D extends InstancedPart> extends AbstractInstancer<D> {

	public final BufferLayout instanceFormat;
	public final StructType<D> structType;
	public final IndirectModel<D> parent;
	int instanceCount = 0;

	boolean anyToUpdate;

	public IndirectInstancer(IndirectModel<D> parent, StructType<D> type) {
		super(type);
		this.parent = parent;
		this.instanceFormat = type.getLayout();
		this.structType = type;
	}

	@Override
	public void notifyDirty() {
		anyToUpdate = true;
	}

	public boolean isEmpty() {
		return !anyToUpdate && !anyToRemove && instanceCount == 0;
	}

	void update() {
		if (anyToRemove) {
			removeDeletedInstances();
		}

		instanceCount = data.size();

		anyToRemove = false;
	}

	@Override
	public void delete() {
		// noop
	}
}
