package com.jozufozu.flywheel.backend.engine.indirect;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.InstanceWriter;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;

public class IndirectInstancer<I extends Instance> extends AbstractInstancer<I> {
	private final long objectStride;
	private final InstanceWriter<I> writer;
	private int modelId;

	public IndirectInstancer(InstanceType<I> type) {
		super(type);
		long instanceStride = type.getLayout()
				.getStride();
		this.objectStride = instanceStride + IndirectBuffers.INT_SIZE;
		writer = this.type.getWriter();
	}

	public void update() {
		removeDeletedInstances();
	}

	public void writeSparse(StagingBuffer stagingBuffer, long start, int dstVbo) {
		int count = instances.size();
		for (int i = changed.nextSetBit(0); i >= 0 && i < count; i = changed.nextSetBit(i + 1)) {
			var instance = instances.get(i);
			stagingBuffer.enqueueCopy(objectStride, dstVbo, start + i * objectStride, ptr -> writeOne(ptr, instance));
		}
		changed.clear();
	}

	public void writeFull(StagingBuffer stagingBuffer, long start, int dstVbo) {
		long totalSize = objectStride * instances.size();

		stagingBuffer.enqueueCopy(totalSize, dstVbo, start, this::writeAll);

		changed.clear();
	}

	private void writeAll(long ptr) {
		for (I instance : instances) {
			writeOne(ptr, instance);
			ptr += objectStride;
		}
	}

	private void writeOne(long ptr, I instance) {
		// write modelID
		MemoryUtil.memPutInt(ptr, modelId);
		// write object
		writer.write(ptr + IndirectBuffers.INT_SIZE, instance);
	}

	public void setModelId(int modelId) {
		this.modelId = modelId;
	}
}
