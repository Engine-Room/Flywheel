package com.jozufozu.flywheel.backend.engine.indirect;

import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;

public class IndirectModel {
	public final IndirectInstancer<?> instancer;
	public final int index;
	private final Vector4fc boundingSphere;

	private int baseInstance = -1;

	public IndirectModel(IndirectInstancer<?> instancer, int index, Vector4fc boundingSphere) {
		this.instancer = instancer;
		this.index = index;
		this.boundingSphere = boundingSphere;
	}

	public int baseInstance() {
		return baseInstance;
	}

	public void prepare(int baseInstance) {
		instancer.update();
		this.baseInstance = baseInstance;
	}

	public void uploadObjects(StagingBuffer stagingBuffer, long start, int dstVbo) {
		instancer.upload(stagingBuffer, start, dstVbo);
	}

	public void write(long ptr) {
		MemoryUtil.memPutInt(ptr, 0); // instanceCount - to be incremented by the cull shader
		MemoryUtil.memPutInt(ptr + 4, baseInstance); // baseInstance
		MemoryUtil.memPutFloat(ptr + 8, boundingSphere.x()); // boundingSphere
		MemoryUtil.memPutFloat(ptr + 12, boundingSphere.y());
		MemoryUtil.memPutFloat(ptr + 16, boundingSphere.z());
		MemoryUtil.memPutFloat(ptr + 20, boundingSphere.w());
	}
}
