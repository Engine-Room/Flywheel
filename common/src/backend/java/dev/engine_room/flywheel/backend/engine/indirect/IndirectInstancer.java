package dev.engine_room.flywheel.backend.engine.indirect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.UnknownNullability;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceWriter;
import dev.engine_room.flywheel.backend.engine.AbstractInstancer;
import dev.engine_room.flywheel.backend.engine.InstancerKey;
import dev.engine_room.flywheel.backend.util.AtomicBitSet;
import dev.engine_room.flywheel.lib.math.MoreMath;

public class IndirectInstancer<I extends Instance> extends AbstractInstancer<I> {
	private final long instanceStride;
	private final InstanceWriter<I> writer;
	private final List<IndirectDraw> associatedDraws = new ArrayList<>();
	private final Vector4fc boundingSphere;

	private final AtomicBitSet changedPages = new AtomicBitSet();

	public ObjectStorage.@UnknownNullability Mapping mapping;

	private int modelIndex = -1;
	private int baseInstance = -1;

	public IndirectInstancer(InstancerKey<I> key, Supplier<AbstractInstancer<I>> recreate) {
		super(key, recreate);
		instanceStride = MoreMath.align4(type.layout()
				.byteSize());
		writer = this.type.writer();
		boundingSphere = key.model().boundingSphere();
	}

	@Override
	public void notifyDirty(int index) {
		if (index < 0 || index >= instanceCount()) {
			return;
		}
		changedPages.set(ObjectStorage.objectIndex2PageIndex(index));
	}

	@Override
	protected void setRangeChanged(int start, int end) {
		super.setRangeChanged(start, end);

		changedPages.set(ObjectStorage.objectIndex2PageIndex(start), ObjectStorage.objectIndex2PageIndex(end) + 1);
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

	public void postUpdate(int modelIndex, int baseInstance) {
		this.modelIndex = modelIndex;
		this.baseInstance = baseInstance;
		mapping.update(modelIndex, instanceCount());
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
		if (changedPages.isEmpty()) {
			return;
		}

		int numPages = mapping.pageCount();

		var instanceCount = instances.size();

		for (int page = changedPages.nextSetBit(0); page >= 0 && page < numPages; page = changedPages.nextSetBit(page + 1)) {
			int startObject = ObjectStorage.pageIndex2ObjectIndex(page);

			if (startObject >= instanceCount) {
				break;
			}

			int endObject = Math.min(instanceCount, ObjectStorage.pageIndex2ObjectIndex(page + 1));

			long baseByte = mapping.page2ByteOffset(page);
			long size = (endObject - startObject) * instanceStride;

			// Because writes are broken into pages, we end up with significantly more calls into
			// StagingBuffer#enqueueCopy and the allocations for the writer got out of hand. Here
			// we've inlined the enqueueCopy call and do not allocate the write lambda at all.
			// Doing so cut upload times in half.

			// Try to write directly into the staging buffer if there is enough contiguous space.
			long direct = stagingBuffer.reserveForCopy(size, instanceVbo, baseByte);

			if (direct != MemoryUtil.NULL) {
				for (int i = startObject; i < endObject; i++) {
					var instance = instances.get(i);
					writer.write(direct, instance);
					direct += instanceStride;
				}
				continue;
			}

			// Otherwise, write to a scratch buffer and enqueue a copy.
			var block = stagingBuffer.getScratch(size);
			var ptr = block.ptr();
			for (int i = startObject; i < endObject; i++) {
				var instance = instances.get(i);
				writer.write(ptr, instance);
				ptr += instanceStride;
			}
			stagingBuffer.enqueueCopy(block.ptr(), size, instanceVbo, baseByte);
		}

		changedPages.clear();
	}

	@Override
	public void delete() {
		for (IndirectDraw draw : draws()) {
			draw.delete();
		}

		mapping.delete();
	}

	public int modelIndex() {
		return modelIndex;
	}

	public int baseInstance() {
		return baseInstance;
	}
}
