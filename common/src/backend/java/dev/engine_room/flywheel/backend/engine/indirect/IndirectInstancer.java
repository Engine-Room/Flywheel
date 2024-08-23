package dev.engine_room.flywheel.backend.engine.indirect;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.instance.InstanceWriter;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.backend.engine.AbstractInstancer;
import dev.engine_room.flywheel.backend.engine.embed.Environment;
import dev.engine_room.flywheel.lib.math.MoreMath;

public class IndirectInstancer<I extends Instance> extends AbstractInstancer<I> {
	private final long instanceStride;
	private final InstanceWriter<I> writer;
	private final List<IndirectDraw> associatedDraws = new ArrayList<>();
	private final Vector4fc boundingSphere;

	public int modelIndex = -1;
	public int baseInstance = -1;
	private int lastModelIndex = -1;
	private int lastBaseInstance = -1;
	private int lastInstanceCount = -1;

	public IndirectInstancer(InstanceType<I> type, Environment environment, Model model) {
		super(type, environment);
		instanceStride = MoreMath.align4(type.layout()
				.byteSize());
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
		MemoryUtil.memPutInt(ptr + 8, environment.matrixIndex()); // matrixIndex
		MemoryUtil.memPutFloat(ptr + 12, boundingSphere.x()); // boundingSphere
		MemoryUtil.memPutFloat(ptr + 16, boundingSphere.y());
		MemoryUtil.memPutFloat(ptr + 20, boundingSphere.z());
		MemoryUtil.memPutFloat(ptr + 24, boundingSphere.w());
	}

	public void uploadInstances(StagingBuffer stagingBuffer, int instanceVbo) {
		long baseByte = baseInstance * instanceStride;

		if (baseInstance != lastBaseInstance) {
			uploadAllInstances(stagingBuffer, baseByte, instanceVbo);
		} else {
			uploadChangedInstances(stagingBuffer, baseByte, instanceVbo);
		}
	}

	public void uploadModelIndices(StagingBuffer stagingBuffer, int modelIndexVbo) {
		long modelIndexBaseByte = baseInstance * IndirectBuffers.INT_SIZE;

		if (baseInstance != lastBaseInstance || modelIndex != lastModelIndex || instances.size() > lastInstanceCount) {
			uploadAllModelIndices(stagingBuffer, modelIndexBaseByte, modelIndexVbo);
		}
	}

	public void resetChanged() {
		lastModelIndex = modelIndex;
		lastBaseInstance = baseInstance;
		lastInstanceCount = instances.size();
		changed.clear();
	}

	private void uploadChangedInstances(StagingBuffer stagingBuffer, long baseByte, int instanceVbo) {
		changed.forEachSetSpan((startInclusive, endInclusive) -> {
			// Generally we're good about ensuring we don't have changed bits set out of bounds, but check just in case
			if (startInclusive >= instances.size()) {
				return;
			}
			int actualEnd = Math.min(endInclusive, instances.size() - 1);

			int instanceCount = actualEnd - startInclusive + 1;
			long totalSize = instanceCount * instanceStride;

			stagingBuffer.enqueueCopy(totalSize, instanceVbo, baseByte + startInclusive * instanceStride, ptr -> {
				for (int i = startInclusive; i <= actualEnd; i++) {
					var instance = instances.get(i);
					writer.write(ptr, instance);
					ptr += instanceStride;
				}
			});
		});
	}

	private void uploadAllInstances(StagingBuffer stagingBuffer, long baseByte, int instanceVbo) {
		long totalSize = instances.size() * instanceStride;

		stagingBuffer.enqueueCopy(totalSize, instanceVbo, baseByte, ptr -> {
			for (I instance : instances) {
				writer.write(ptr, instance);
				ptr += instanceStride;
			}
		});
	}

	private void uploadAllModelIndices(StagingBuffer stagingBuffer, long modelIndexBaseByte, int modelIndexVbo) {
		long modelIndexTotalSize = instances.size() * IndirectBuffers.INT_SIZE;

		stagingBuffer.enqueueCopy(modelIndexTotalSize, modelIndexVbo, modelIndexBaseByte, ptr -> {
			for (int i = 0; i < instances.size(); i++) {
				MemoryUtil.memPutInt(ptr, modelIndex);
				ptr += IndirectBuffers.INT_SIZE;
			}
		});
	}

	@Override
	public void delete() {
		for (IndirectDraw draw : draws()) {
			draw.delete();
		}
	}
}
