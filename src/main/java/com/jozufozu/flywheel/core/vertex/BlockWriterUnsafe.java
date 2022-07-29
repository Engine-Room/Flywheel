package com.jozufozu.flywheel.core.vertex;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.util.RenderMath;

public class BlockWriterUnsafe extends VertexWriterUnsafe<BlockVertex> {

	public BlockWriterUnsafe(BlockVertex type, ByteBuffer buffer) {
		super(type, buffer);
	}

	@Override
	public void writeVertex(VertexList list, int i) {
		float x = list.x(i);
		float y = list.y(i);
		float z = list.z(i);

		float xN = list.normalX(i);
		float yN = list.normalY(i);
		float zN = list.normalZ(i);

		float u = list.u(i);
		float v = list.v(i);

		byte r = list.r(i);
		byte g = list.g(i);
		byte b = list.b(i);
		byte a = list.a(i);

		int light = list.light(i);

		putVertex(x, y, z, u, v, r, g, b, a, light, xN, yN, zN);
	}

	public void putVertex(float x, float y, float z, float u, float v, byte r, byte g, byte b, byte a, int light, float nX, float nY, float nZ) {
		MemoryUtil.memPutFloat(ptr, x);
		MemoryUtil.memPutFloat(ptr + 4, y);
		MemoryUtil.memPutFloat(ptr + 8, z);
		MemoryUtil.memPutByte(ptr + 12, r);
		MemoryUtil.memPutByte(ptr + 13, g);
		MemoryUtil.memPutByte(ptr + 14, b);
		MemoryUtil.memPutByte(ptr + 15, a);
		MemoryUtil.memPutFloat(ptr + 16, u);
		MemoryUtil.memPutFloat(ptr + 20, v);
		MemoryUtil.memPutInt(ptr + 24, (light >> 4) & 0xF000F);
		MemoryUtil.memPutByte(ptr + 28, RenderMath.nb(nX));
		MemoryUtil.memPutByte(ptr + 29, RenderMath.nb(nY));
		MemoryUtil.memPutByte(ptr + 30, RenderMath.nb(nZ));

		ptr += 32;
	}
}
