package com.jozufozu.flywheel.backend.engine.indirect;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.InstanceWriter;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;

public class IndirectInstancer<I extends Instance> extends AbstractInstancer<I> {
	private final long instanceStride;

	public IndirectInstancer(InstanceType<I> type) {
		super(type);
		this.instanceStride = type.getLayout()
				.getStride();
	}

	public void update() {
		removeDeletedInstances();
	}

	public void writeSparse(long objectPtr, long batchIDPtr, int batchID) {
		int count = instances.size();
		InstanceWriter<I> writer = type.getWriter();
		for (int i = changed.nextSetBit(0); i >= 0 && i < count; i = changed.nextSetBit(i + 1)) {
			// write object
			writer.write(objectPtr + instanceStride * i, instances.get(i));

			// write batchID
			MemoryUtil.memPutInt(batchIDPtr + IndirectBuffers.INT_SIZE * i, batchID);
		}
		changed.clear();
	}

	public void writeFull(long objectPtr, long batchIDPtr, int batchID) {
		InstanceWriter<I> writer = type.getWriter();
		for (I object : instances) {
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
