package com.jozufozu.flywheel.core.vertex;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.util.RenderMath;

public class PosTexNormalWriter {

	private long addr;

	private int vertexCount;

	public PosTexNormalWriter(ByteBuffer buffer) {
		addr = MemoryUtil.memAddress(buffer);
	}

	public void putVertex(float x, float y, float z, float nX, float nY, float nZ, float u, float v) {
		MemoryUtil.memPutFloat(addr, x);
		MemoryUtil.memPutFloat(addr + 4, y);
		MemoryUtil.memPutFloat(addr + 8, z);
		MemoryUtil.memPutFloat(addr + 12, u);
		MemoryUtil.memPutFloat(addr + 16, v);
		MemoryUtil.memPutByte(addr + 20, RenderMath.nb(nX));
		MemoryUtil.memPutByte(addr + 21, RenderMath.nb(nY));
		MemoryUtil.memPutByte(addr + 22, RenderMath.nb(nZ));

		addr += 23;
		vertexCount++;
	}

	public int getVertexCount() {
		return vertexCount;
	}
}
