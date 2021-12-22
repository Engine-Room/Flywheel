package com.jozufozu.flywheel.core.vertex;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.backend.gl.attrib.CommonAttributes;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.util.RenderMath;

import net.minecraft.client.renderer.LightTexture;

public class BlockVertex implements VertexType {

	public static final VertexFormat FORMAT = VertexFormat.builder()
			.addAttributes(CommonAttributes.VEC3,
					CommonAttributes.UV,
					CommonAttributes.RGBA,
					CommonAttributes.LIGHT,
					CommonAttributes.NORMAL)
			.build();

	@Override
	public VertexFormat getFormat() {
		return FORMAT;
	}

	@Override
	public void copyInto(ByteBuffer buffer, VertexList reader) {
		int stride = getStride();
		long addr = MemoryUtil.memAddress(buffer);

		int vertexCount = reader.getVertexCount();
		for (int i = 0; i < vertexCount; i++) {
			float x = reader.getX(i);
			float y = reader.getY(i);
			float z = reader.getZ(i);

			float xN = reader.getNX(i);
			float yN = reader.getNY(i);
			float zN = reader.getNZ(i);

			float u = reader.getU(i);
			float v = reader.getV(i);

			byte r = reader.getR(i);
			byte g = reader.getG(i);
			byte b = reader.getB(i);
			byte a = reader.getA(i);

			int light = reader.getLight(i);

			MemoryUtil.memPutFloat(addr, x);
			MemoryUtil.memPutFloat(addr + 4, y);
			MemoryUtil.memPutFloat(addr + 8, z);
			MemoryUtil.memPutFloat(addr + 12, u);
			MemoryUtil.memPutFloat(addr + 16, v);
			MemoryUtil.memPutByte(addr + 20, r);
			MemoryUtil.memPutByte(addr + 21, g);
			MemoryUtil.memPutByte(addr + 22, b);
			MemoryUtil.memPutByte(addr + 23, a);
			MemoryUtil.memPutByte(addr + 24, (byte) (LightTexture.block(light) << 4));
			MemoryUtil.memPutByte(addr + 25, (byte) (LightTexture.sky(light) << 4));
			MemoryUtil.memPutByte(addr + 26, RenderMath.nb(xN));
			MemoryUtil.memPutByte(addr + 27, RenderMath.nb(yN));
			MemoryUtil.memPutByte(addr + 28, RenderMath.nb(zN));

			addr += stride;
		}
	}
}
