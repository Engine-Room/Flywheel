package com.jozufozu.flywheel.lib.modelpart;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.util.RenderMath;

public class VertexWriterImpl implements VertexWriter {
	private long ptr;

	public VertexWriterImpl(long ptr) {
		this.ptr = ptr;
	}

	@Override
	public void putVertex(float x, float y, float z, float u, float v, float nX, float nY, float nZ) {
		MemoryUtil.memPutFloat(ptr, x);
		MemoryUtil.memPutFloat(ptr + 4, y);
		MemoryUtil.memPutFloat(ptr + 8, z);
		MemoryUtil.memPutFloat(ptr + 12, u);
		MemoryUtil.memPutFloat(ptr + 16, v);
		MemoryUtil.memPutByte(ptr + 20, RenderMath.nb(nX));
		MemoryUtil.memPutByte(ptr + 21, RenderMath.nb(nY));
		MemoryUtil.memPutByte(ptr + 22, RenderMath.nb(nZ));

		ptr += 23;
	}
}
