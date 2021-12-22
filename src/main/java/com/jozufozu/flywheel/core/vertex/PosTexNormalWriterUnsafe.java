package com.jozufozu.flywheel.core.vertex;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.util.RenderMath;

public class PosTexNormalWriterUnsafe extends VertexWriterUnsafe<PosTexNormalVertex> {

	public PosTexNormalWriterUnsafe(PosTexNormalVertex type, ByteBuffer buffer) {
		super(type, buffer);
	}

	@Override
	public void writeVertex(VertexList list, int i) {
		float x = list.getX(i);
		float y = list.getY(i);
		float z = list.getZ(i);

		float u = list.getU(i);
		float v = list.getV(i);

		float xN = list.getNX(i);
		float yN = list.getNY(i);
		float zN = list.getNZ(i);

		putVertex(x, y, z, xN, yN, zN, u, v);
	}

	public void putVertex(float x, float y, float z, float nX, float nY, float nZ, float u, float v) {
		MemoryUtil.memPutFloat(ptr, x);
		MemoryUtil.memPutFloat(ptr + 4, y);
		MemoryUtil.memPutFloat(ptr + 8, z);
		MemoryUtil.memPutFloat(ptr + 12, u);
		MemoryUtil.memPutFloat(ptr + 16, v);
		MemoryUtil.memPutByte(ptr + 20, RenderMath.nb(nX));
		MemoryUtil.memPutByte(ptr + 21, RenderMath.nb(nY));
		MemoryUtil.memPutByte(ptr + 22, RenderMath.nb(nZ));

		ptr += 23;
		advance();
	}
}
