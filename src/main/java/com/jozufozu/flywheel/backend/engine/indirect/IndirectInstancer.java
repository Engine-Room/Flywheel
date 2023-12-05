package com.jozufozu.flywheel.backend.engine.indirect;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.InstanceWriter;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;

public class IndirectInstancer<I extends Instance> extends AbstractInstancer<I> {
	private final long instanceStride;
	private final long objectStride;
	private final InstanceWriter<I> writer;

	public IndirectInstancer(InstanceType<I> type) {
		super(type);
		this.instanceStride = type.getLayout()
				.getStride();
		this.objectStride = instanceStride + IndirectBuffers.INT_SIZE;
		writer = this.type.getWriter();
	}

	public void update() {
		removeDeletedInstances();
	}

	public void writeSparse(StagingBuffer stagingBuffer, long start, int modelID, int dstVbo) {
		int count = instances.size();
		// Backup buffer for when we can't write to the staging buffer.
		MemoryBlock backup = null;
		for (int i = changed.nextSetBit(0); i >= 0 && i < count; i = changed.nextSetBit(i + 1)) {
			long ptr = stagingBuffer.reserveForTransferTo(objectStride, dstVbo, start + i * objectStride);
			if (ptr == MemoryUtil.NULL) {
				// Staging buffer can't fit this object, so we'll have to write it to a backup buffer.
				if (backup == null) {
					backup = MemoryBlock.malloc(objectStride);
				}
				writeOne(backup.ptr(), instances.get(i), modelID);

				stagingBuffer.enqueueCopy(backup.ptr(), objectStride, dstVbo, start + i * objectStride);
			} else {
				writeOne(ptr, instances.get(i), modelID);
			}
		}
		changed.clear();

		// Free the backup buffer if we allocated one.
		if (backup != null) {
			backup.free();
		}
	}

	public void writeFull(StagingBuffer stagingBuffer, long start, int modelID, int dstVbo) {
		long totalSize = objectStride * instances.size();

		long ptr = stagingBuffer.reserveForTransferTo(totalSize, dstVbo, start);

		if (ptr != MemoryUtil.NULL) {
			writeAll(ptr, modelID);
		} else {
			var block = MemoryBlock.malloc(totalSize);
			writeAll(block.ptr(), modelID);
			stagingBuffer.enqueueCopy(block.ptr(), totalSize, dstVbo, start);
			block.free();
		}

		changed.clear();
	}

	private void writeAll(long ptr, int modelID) {
		for (I instance : instances) {
			writeOne(ptr, instance, modelID);
			ptr += objectStride;
		}
	}

	private void writeOne(long ptr, I instance, int modelID) {
		// write modelID
		MemoryUtil.memPutInt(ptr, modelID);
		// write object
		writer.write(ptr + IndirectBuffers.INT_SIZE, instance);
	}
}
