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
import dev.engine_room.flywheel.backend.util.AtomicBitSet;
import dev.engine_room.flywheel.lib.math.MoreMath;

public class IndirectInstancer<I extends Instance> extends AbstractInstancer<I> {
	private final long instanceStride;
	private final InstanceWriter<I> writer;
	private final List<IndirectDraw> associatedDraws = new ArrayList<>();
	private final Vector4fc boundingSphere;

	private final AtomicBitSet changedPages = new AtomicBitSet();

	public InstancePager.Allocation pageFile;

	private int modelIndex = -1;
	private int baseInstance = -1;

	public IndirectInstancer(InstanceType<I> type, Environment environment, Model model) {
		super(type, environment);
		instanceStride = MoreMath.align4(type.layout()
				.byteSize());
		writer = this.type.writer();
		boundingSphere = model.boundingSphere();
	}

	@Override
	public void notifyDirty(int index) {
		if (index < 0 || index >= instanceCount()) {
			return;
		}
		changed.set(index);
		changedPages.set(InstancePager.object2Page(index));
	}

	@Override
	protected void setRangeChanged(int start, int end) {
		super.setRangeChanged(start, end);

		changedPages.set(InstancePager.object2Page(start), InstancePager.object2Page(end) + 1);
	}

	@Override
	protected void clearChangedRange(int start, int end) {
		super.clearChangedRange(start, end);

		// changedPages.clear(pageFile.object2Page(start), pageFile);
	}

	public void addDraw(IndirectDraw draw) {
		associatedDraws.add(draw);
	}

	public List<IndirectDraw> draws() {
		return associatedDraws;
	}

	public void update() {
		removeDeletedInstances();

		pageFile.activeCount(instanceCount());
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
		int numPages = pageFile.pageCount();

		var instanceCount = instances.size();

		for (int page = changedPages.nextSetBit(0); page >= 0 && page < numPages; page = changedPages.nextSetBit(page + 1)) {
			int startObject = InstancePager.page2Object(page);

			if (startObject >= instanceCount) {
				break;
			}

			int endObject = Math.min(instanceCount, InstancePager.page2Object(page + 1));

			long baseByte = pageFile.page2ByteOffset(page);
			long size = (endObject - startObject) * instanceStride;

			stagingBuffer.enqueueCopy(size, instanceVbo, baseByte, ptr -> {
				for (int i = startObject; i < endObject; i++) {
					writer.write(ptr, instances.get(i));
					ptr += instanceStride;
				}
			});
		}

		changed.clear();
		changedPages.clear();
	}

	@Override
	public void delete() {
		for (IndirectDraw draw : draws()) {
			draw.delete();
		}
	}

	public void modelIndex(int modelIndex) {
		this.modelIndex = modelIndex;
		pageFile.modelIndex(modelIndex);
	}

	public int modelIndex() {
		return modelIndex;
	}

	public void baseInstance(int baseInstance) {
		this.baseInstance = baseInstance;
	}

	public int baseInstance() {
		return baseInstance;
	}
}
