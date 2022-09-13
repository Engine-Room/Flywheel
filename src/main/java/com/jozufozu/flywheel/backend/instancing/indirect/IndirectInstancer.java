package com.jozufozu.flywheel.backend.instancing.indirect;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.backend.instancing.AbstractInstancer;

public class IndirectInstancer<D extends InstancedPart> extends AbstractInstancer<D> {

	private final long objectStride;
	private final StructWriter<D> writer;
	int instanceCount = 0;

	boolean anyToUpdate;

	public IndirectInstancer(StructType<D> type) {
		super(type);
		this.objectStride = type.getLayout()
				.getStride();
		writer = type.getWriter();
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

	public void writeSparse(long objectPtr, long batchIDPtr, int batchID) {
		for (int i = 0, size = data.size(); i < size; i++) {
			final var element = data.get(i);
			if (element.checkDirtyAndClear()) {
				writer.write(objectPtr + i * objectStride, element);

				MemoryUtil.memPutInt(batchIDPtr + i * IndirectBuffers.INT_SIZE, batchID);
			}
		}
	}

	public void writeFull(long objectPtr, long batchIDPtr, int batchID) {
		for (var object : data) {
			// write object
			writer.write(objectPtr, object);
			objectPtr += objectStride;

			// write batchID
			MemoryUtil.memPutInt(batchIDPtr, batchID);
			batchIDPtr += IndirectBuffers.INT_SIZE;
		}
	}

	@Override
	public void delete() {
		// noop
	}
}
