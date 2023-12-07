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

	public void writeChanged(long objectPtr, int modelIndex) {
		int count = instances.size();
		InstanceWriter<I> writer = type.getWriter();
		for (int i = changed.nextSetBit(0); i >= 0 && i < count; i = changed.nextSetBit(i + 1)) {
			long ptr = objectPtr + objectStride * i;
			MemoryUtil.memPutInt(ptr, modelIndex); // modelIndex
			writer.write(ptr + IndirectBuffers.INT_SIZE, instances.get(i)); // instance
		}
		changed.clear();
	}

	public void writeAll(long objectPtr, int modelIndex) {
		InstanceWriter<I> writer = type.getWriter();
		for (I object : instances) {
			MemoryUtil.memPutInt(objectPtr, modelIndex); // modelIndex
			objectPtr += IndirectBuffers.INT_SIZE;

			writer.write(objectPtr, object); // instance
			objectPtr += instanceStride;
		}
		changed.clear();
	}
}
