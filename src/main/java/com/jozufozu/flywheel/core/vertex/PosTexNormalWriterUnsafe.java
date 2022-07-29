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
		float x = list.x(i);
		float y = list.y(i);
		float z = list.z(i);

		float u = list.u(i);
		float v = list.v(i);

		float xN = list.normalX(i);
		float yN = list.normalY(i);
		float zN = list.normalZ(i);

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
	}
}
