package com.jozufozu.flywheel.core.model;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.vertex.VertexType;
import com.jozufozu.flywheel.util.ModelReader;
import com.jozufozu.flywheel.util.RenderMath;

import net.minecraft.client.renderer.LightTexture;

public class BlockType implements VertexType {

	public static final BlockType INSTANCE = new BlockType();

	@Override
	public VertexFormat getFormat() {
		return Formats.COLORED_LIT_MODEL;
	}

	@Override
	public void copyInto(ByteBuffer buffer, ModelReader reader) {
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
			MemoryUtil.memPutByte(addr + 12, RenderMath.nb(xN));
			MemoryUtil.memPutByte(addr + 13, RenderMath.nb(yN));
			MemoryUtil.memPutByte(addr + 14, RenderMath.nb(zN));
			MemoryUtil.memPutFloat(addr + 15, u);
			MemoryUtil.memPutFloat(addr + 19, v);
			MemoryUtil.memPutByte(addr + 23, r);
			MemoryUtil.memPutByte(addr + 24, g);
			MemoryUtil.memPutByte(addr + 25, b);
			MemoryUtil.memPutByte(addr + 26, a);

			byte block = (byte) (LightTexture.block(light) << 4);
			byte sky = (byte) (LightTexture.sky(light) << 4);

			MemoryUtil.memPutByte(addr + 27, block);
			MemoryUtil.memPutByte(addr + 28, sky);

			addr += stride;
		}
	}
}
