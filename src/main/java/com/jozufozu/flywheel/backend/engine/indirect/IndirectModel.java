package com.jozufozu.flywheel.backend.engine.indirect;

import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;

public class IndirectModel {
	public final IndirectInstancer<?> instancer;
	public final int id;

	public int baseInstance = -1;
	private boolean needsFullWrite = true;

	private final Vector4fc boundingSphere;

	public IndirectModel(IndirectInstancer<?> instancer, int id, Vector4f boundingSphere) {
		this.instancer = instancer;
		this.id = id;
		this.boundingSphere = boundingSphere;
	}

	public void writeModel(long ptr) {
		MemoryUtil.memPutInt(ptr, 0); // instanceCount - to be incremented by the compute shader
		MemoryUtil.memPutInt(ptr + 4, baseInstance); // baseInstance
		boundingSphere.getToAddress(ptr + 8); // boundingSphere
	}

	public void prepare(int baseInstance) {
		instancer.update();
		if (baseInstance == this.baseInstance) {
			needsFullWrite = false;
			return;
		}
		this.baseInstance = baseInstance;
		needsFullWrite = true;
	}

	public void writeObjects(StagingBuffer stagingBuffer, long start, int dstVbo) {
		if (needsFullWrite) {
			instancer.writeFull(stagingBuffer, start, dstVbo);
		} else {
			instancer.writeSparse(stagingBuffer, start, dstVbo);
		}
	}
}
