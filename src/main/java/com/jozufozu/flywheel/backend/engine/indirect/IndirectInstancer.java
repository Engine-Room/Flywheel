package com.jozufozu.flywheel.backend.engine.indirect;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.InstanceWriter;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.backend.context.Context;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;

public class IndirectInstancer<I extends Instance> extends AbstractInstancer<I> {
	private final long objectStride;
	private final InstanceWriter<I> writer;
	private final List<IndirectDraw> associatedDraws = new ArrayList<>();
	private final Vector4fc boundingSphere;


	public int index;
	public int baseInstance = -1;
	private int lastModelIndex = -1;
	private long lastStartPos = -1;

	public IndirectInstancer(InstanceType<I> type, Context context, Model model) {
		super(type, context);
		this.objectStride = type.layout()
				.byteSize() + IndirectBuffers.INT_SIZE;
		writer = this.type.writer();
		boundingSphere = model.boundingSphere();
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

	public void writeModel(long ptr) {
		MemoryUtil.memPutInt(ptr, 0); // instanceCount - to be incremented by the cull shader
		MemoryUtil.memPutInt(ptr + 4, baseInstance); // baseInstance
		MemoryUtil.memPutFloat(ptr + 8, boundingSphere.x()); // boundingSphere
		MemoryUtil.memPutFloat(ptr + 12, boundingSphere.y());
		MemoryUtil.memPutFloat(ptr + 16, boundingSphere.z());
		MemoryUtil.memPutFloat(ptr + 20, boundingSphere.w());
	}

	public void uploadObjects(StagingBuffer stagingBuffer, long startPos, int dstVbo) {
        if (shouldUploadAll(startPos)) {
			uploadAll(stagingBuffer, startPos, dstVbo);
		} else {
			uploadChanged(stagingBuffer, startPos, dstVbo);
		}

		changed.clear();
		lastStartPos = startPos;
		lastModelIndex = index;
	}

	private boolean shouldUploadAll(long startPos) {
		return startPos != lastStartPos || index != lastModelIndex;
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
		MemoryUtil.memPutInt(ptr, index);
		writer.write(ptr + IndirectBuffers.INT_SIZE, instance);
	}
}
