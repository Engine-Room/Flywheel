package com.jozufozu.flywheel.backend.engine.indirect;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.InstanceWriter;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;

public class IndirectInstancer<I extends Instance> extends AbstractInstancer<I> {
	private final long objectStride;
	private final InstanceWriter<I> writer;
	private int modelIndex;

	private long lastStartPos = -1;

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

	public void write(StagingBuffer stagingBuffer, long startPos, int dstVbo) {
		if (shouldWriteAll(startPos)) {
			writeAll(stagingBuffer, startPos, dstVbo);
		} else {
			writeChanged(stagingBuffer, startPos, dstVbo);
		}

		changed.clear();
		lastStartPos = startPos;
	}

	private boolean shouldWriteAll(long startPos) {
		// If enough of the buffer has changed, write the whole thing to avoid the overhead of a bunch of small writes.
		return startPos != lastStartPos || moreThanTwoThirdsChanged();
	}

	private boolean moreThanTwoThirdsChanged() {
		return (changed.cardinality() * 3) > (instances.size() * 2);
	}

	private void writeChanged(StagingBuffer stagingBuffer, long start, int dstVbo) {
		int count = instances.size();
		for (int i = changed.nextSetBit(0); i >= 0 && i < count; i = changed.nextSetBit(i + 1)) {
			var instance = instances.get(i);
			stagingBuffer.enqueueCopy(objectStride, dstVbo, start + i * objectStride, ptr -> writeOne(ptr, instance));
		}
	}

	private void writeAll(StagingBuffer stagingBuffer, long start, int dstVbo) {
		long totalSize = objectStride * instances.size();

		stagingBuffer.enqueueCopy(totalSize, dstVbo, start, this::writeAll);
	}

	private void writeAll(long ptr) {
		for (I instance : instances) {
			writeOne(ptr, instance);
			ptr += objectStride;
		}
	}

	private void writeOne(long ptr, I instance) {
		MemoryUtil.memPutInt(ptr, modelIndex);
		writer.write(ptr + IndirectBuffers.INT_SIZE, instance);
	}

	public void setModelIndex(int modelIndex) {
		this.modelIndex = modelIndex;
	}
}
