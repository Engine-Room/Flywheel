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
		float x = list.getX(i);
		float y = list.getY(i);
		float z = list.getZ(i);

		float xN = list.getNX(i);
		float yN = list.getNY(i);
		float zN = list.getNZ(i);

		float u = list.getU(i);
		float v = list.getV(i);

		byte r = list.getR(i);
		byte g = list.getG(i);
		byte b = list.getB(i);
		byte a = list.getA(i);

		int light = list.getLight(i);

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
		MemoryUtil.memPutInt(ptr + 24, light << 8); // light is packed in the low byte of each short
		MemoryUtil.memPutByte(ptr + 28, RenderMath.nb(nX));
		MemoryUtil.memPutByte(ptr + 29, RenderMath.nb(nY));
		MemoryUtil.memPutByte(ptr + 30, RenderMath.nb(nZ));

		ptr += 32;
		advance();
	}
}
