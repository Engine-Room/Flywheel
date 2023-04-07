package com.jozufozu.flywheel.backend.engine.indirect;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.struct.InstancePart;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;

public class IndirectInstancer<P extends InstancePart> extends AbstractInstancer<P> {
	private final long instanceStride;

	public IndirectInstancer(StructType<P> type) {
		super(type);
		this.instanceStride = type.getLayout()
				.getStride();
	}

	public void update() {
		removeDeletedInstances();
	}

	public void writeSparse(long objectPtr, long batchIDPtr, int batchID) {
		int count = data.size();
		StructWriter<P> writer = type.getWriter();
		for (int i = changed.nextSetBit(0); i >= 0 && i < count; i = changed.nextSetBit(i + 1)) {
			// write object
			writer.write(objectPtr + instanceStride * i, data.get(i));

			// write batchID
			MemoryUtil.memPutInt(batchIDPtr + IndirectBuffers.INT_SIZE * i, batchID);
		}
		changed.clear();
	}

	public void writeFull(long objectPtr, long batchIDPtr, int batchID) {
		StructWriter<P> writer = type.getWriter();
		for (var object : data) {
			// write object
			writer.write(objectPtr, object);
			objectPtr += instanceStride;

			// write batchID
			MemoryUtil.memPutInt(batchIDPtr, batchID);
			batchIDPtr += IndirectBuffers.INT_SIZE;
		}
		changed.clear();
	}
}
