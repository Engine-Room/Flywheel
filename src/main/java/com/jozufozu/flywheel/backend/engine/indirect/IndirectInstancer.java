package com.jozufozu.flywheel.backend.engine.indirect;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.InstanceWriter;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;
import com.jozufozu.flywheel.backend.engine.embed.Environment;

public class IndirectInstancer<I extends Instance> extends AbstractInstancer<I> {
	private final long instanceStride;
	private final InstanceWriter<I> writer;
	private final List<IndirectDraw> associatedDraws = new ArrayList<>();
	private final Vector4fc boundingSphere;

	public int index = -1;
	public int baseInstance = -1;
	private int lastModelIndex = -1;
	private int lastBaseInstance = -1;

	public IndirectInstancer(InstanceType<I> type, Environment environment, Model model) {
		super(type, environment);
		instanceStride = type.layout()
				.byteSize();
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

	public void uploadInstances(StagingBuffer stagingBuffer, int instanceVbo, int modelIndexVbo) {
		long baseByte = baseInstance * instanceStride;
		long modelIndexBaseByte = baseInstance * IndirectBuffers.INT_SIZE;

        if (shouldUploadAll()) {
			uploadAll(stagingBuffer, baseByte, modelIndexBaseByte, instanceVbo, modelIndexVbo);
		} else {
			uploadChanged(stagingBuffer, baseByte, modelIndexBaseByte, instanceVbo, modelIndexVbo);
		}

		changed.clear();
		lastModelIndex = index;
		lastBaseInstance = baseInstance;
	}

	private boolean shouldUploadAll() {
		return baseInstance != lastBaseInstance || index != lastModelIndex;
	}

	private void uploadChanged(StagingBuffer stagingBuffer, long baseByte, long modelIndexBaseByte, int instanceVbo, int modelIndexVbo) {
		changed.forEachSetSpan((startInclusive, endInclusive) -> {
			int instanceCount = endInclusive - startInclusive + 1;
			long totalSize = instanceCount * instanceStride;
			long modelIndexTotalSize = instanceCount * IndirectBuffers.INT_SIZE;

			stagingBuffer.enqueueCopy(totalSize, instanceVbo, baseByte + startInclusive * instanceStride, ptr -> {
				for (int i = startInclusive; i <= endInclusive; i++) {
					var instance = instances.get(i);
					writer.write(ptr, instance);
					ptr += instanceStride;
				}
			});

			stagingBuffer.enqueueCopy(modelIndexTotalSize, modelIndexVbo, modelIndexBaseByte + startInclusive * IndirectBuffers.INT_SIZE, ptr -> {
				for (int i = startInclusive; i <= endInclusive; i++) {
					MemoryUtil.memPutInt(ptr, index);
					ptr += IndirectBuffers.INT_SIZE;
				}
			});
		});
	}

	private void uploadAll(StagingBuffer stagingBuffer, long baseByte, long modelIndexBaseByte, int instanceVbo, int modelIndexVbo) {
		long totalSize = instances.size() * instanceStride;
		long modelIndexTotalSize = instances.size() * IndirectBuffers.INT_SIZE;

		stagingBuffer.enqueueCopy(totalSize, instanceVbo, baseByte, ptr -> {
			for (I instance : instances) {
				writer.write(ptr, instance);
				ptr += instanceStride;
			}
		});

		stagingBuffer.enqueueCopy(modelIndexTotalSize, modelIndexVbo, modelIndexBaseByte, ptr -> {
			for (int i = 0; i < instances.size(); i++) {
				MemoryUtil.memPutInt(ptr, index);
				ptr += IndirectBuffers.INT_SIZE;
			}
		});
	}

	@Override
	public void delete() {
		super.delete();

		for (IndirectDraw draw : draws()) {
			draw.delete();
		}
	}
}
