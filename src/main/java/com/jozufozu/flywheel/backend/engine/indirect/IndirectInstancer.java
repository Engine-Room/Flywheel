package com.jozufozu.flywheel.backend.engine.indirect;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.instancer.InstancePart;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;

public class IndirectInstancer<P extends InstancePart> extends AbstractInstancer<P> {

	private final long objectStride;
	private final StructWriter<P> writer;
	int instanceCount = 0;

	public IndirectInstancer(StructType<P> type) {
		super(type);
		this.objectStride = type.getLayout()
				.getStride();
		writer = type.getWriter();
	}

	public boolean isEmpty() {
		return changed.isEmpty() && deleted.isEmpty() && instanceCount == 0;
	}

	void update() {
		if (!deleted.isEmpty()) {
			removeDeletedInstances();
		}

		instanceCount = data.size();
	}

	public void writeSparse(long objectPtr, long batchIDPtr, int batchID) {
		final int size = data.size();

		for (int i = changed.nextSetBit(0); i >= 0 && i < size; i = changed.nextSetBit(i + 1)) {
			writer.write(objectPtr + i * objectStride, data.get(i));

			MemoryUtil.memPutInt(batchIDPtr + i * IndirectBuffers.INT_SIZE, batchID);
		}
		changed.clear();
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
		changed.clear();
	}
}
