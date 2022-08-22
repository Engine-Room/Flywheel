package com.jozufozu.flywheel.backend.instancing.indirect;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.instancing.AbstractInstancer;
import com.jozufozu.flywheel.core.layout.BufferLayout;

public class IndirectInstancer<D extends InstancedPart> extends AbstractInstancer<D> {

	public final BufferLayout instanceFormat;
	public final IndirectModel<D> parent;
	int instanceCount = 0;

	boolean anyToUpdate;

	public IndirectInstancer(IndirectModel<D> parent, StructType<D> type) {
		super(type);
		this.parent = parent;
		this.instanceFormat = type.getLayout();
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
		var storageBufferWriter = this.type.getStorageBufferWriter();
		long objectStride = storageBufferWriter.getAlignment();
		for (int i = 0, size = data.size(); i < size; i++) {
			final var element = data.get(i);
			if (element.checkDirtyAndClear()) {
				storageBufferWriter.write(objectPtr + i * objectStride, element);

				MemoryUtil.memPutInt(batchIDPtr + i * IndirectBuffers.INT_SIZE, batchID);
			}
		}
	}

	public void writeFull(long objectPtr, long batchIDPtr, int batchID) {
		var storageBufferWriter = this.type.getStorageBufferWriter();
		var objectStride = storageBufferWriter.getAlignment();
		for (var object : data) {
			// write object
			storageBufferWriter.write(objectPtr, object);
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
