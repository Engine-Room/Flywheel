package com.jozufozu.flywheel.backend.engine.indirect;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.InstanceWriter;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;

public class IndirectInstancer<I extends Instance> extends AbstractInstancer<I> {
	private final long instanceStride;
	private final long objectStride;

	public IndirectInstancer(InstanceType<I> type) {
		super(type);
		this.instanceStride = type.getLayout()
				.getStride();
		this.objectStride = instanceStride + IndirectBuffers.INT_SIZE;
	}

	public void update() {
		removeDeletedInstances();
	}

	public void writeSparse(long objectPtr, int batchID) {
		int count = instances.size();
		InstanceWriter<I> writer = type.getWriter();
		for (int i = changed.nextSetBit(0); i >= 0 && i < count; i = changed.nextSetBit(i + 1)) {
			long ptr = objectPtr + objectStride * i;
			// write batchID
			MemoryUtil.memPutInt(ptr, batchID);
			// write object
			writer.write(ptr + IndirectBuffers.INT_SIZE, instances.get(i));
		}
		changed.clear();
	}

	public void writeFull(long objectPtr, int modelID) {
		InstanceWriter<I> writer = type.getWriter();
		for (I object : instances) {
			// write modelID
			MemoryUtil.memPutInt(objectPtr, modelID);
			objectPtr += IndirectBuffers.INT_SIZE;

			// write object
			writer.write(objectPtr, object);
			objectPtr += instanceStride;
		}
		changed.clear();
	}
}
