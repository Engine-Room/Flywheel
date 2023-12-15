package com.jozufozu.flywheel.backend.engine.indirect;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.InstanceWriter;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;

public class IndirectInstancer<I extends Instance> extends AbstractInstancer<I> {
	private final long objectStride;
	private final InstanceWriter<I> writer;
	private final List<IndirectDraw> associatedDraws = new ArrayList<>();
	private int modelIndex;

	private long lastStartPos = -1;

	public IndirectInstancer(InstanceType<I> type) {
		super(type);
		long instanceStride = type.getLayout()
				.getStride();
		this.objectStride = instanceStride + IndirectBuffers.INT_SIZE;
		writer = this.type.getWriter();
	}

	public void addDraw(IndirectDraw draw) {
		associatedDraws.add(draw);
	}

	public List<IndirectDraw> draws() {
		return associatedDraws;
	}

	public void update() {
		removeDeletedInstances();
	}

	public void upload(StagingBuffer stagingBuffer, long startPos, int dstVbo) {
		if (shouldUploadAll(startPos)) {
			uploadAll(stagingBuffer, startPos, dstVbo);
		} else {
			uploadChanged(stagingBuffer, startPos, dstVbo);
		}

		changed.clear();
		lastStartPos = startPos;
	}

	private boolean shouldUploadAll(long startPos) {
		return startPos != lastStartPos;
	}

	private void uploadChanged(StagingBuffer stagingBuffer, long baseByte, int dstVbo) {
		changed.forEachSetSpan((startInclusive, endInclusive) -> {
			var totalSize = (endInclusive - startInclusive + 1) * objectStride;

			stagingBuffer.enqueueCopy(totalSize, dstVbo, baseByte + startInclusive * objectStride, ptr -> {
				for (int i = startInclusive; i <= endInclusive; i++) {
					var instance = instances.get(i);
					writeOne(ptr, instance);
					ptr += objectStride;
				}
			});
		});
	}

	private void uploadAll(StagingBuffer stagingBuffer, long start, int dstVbo) {
		long totalSize = objectStride * instances.size();

		stagingBuffer.enqueueCopy(totalSize, dstVbo, start, this::uploadAll);
	}

	private void uploadAll(long ptr) {
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
