package com.jozufozu.flywheel.core.vertex;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.backend.gl.attrib.CommonAttributes;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;

public class PosTexNormalVertex implements VertexType {

	public static final VertexFormat FORMAT = VertexFormat.builder()
			.addAttributes(CommonAttributes.VEC3, CommonAttributes.UV, CommonAttributes.NORMAL)
			.build();

	@Override
	public VertexFormat getFormat() {
		return FORMAT;
	}

	@Override
	public void copyInto(ByteBuffer buffer, VertexList reader) {
		PosTexNormalWriter writer = new PosTexNormalWriter(buffer);

		int vertexCount = reader.getVertexCount();
		for (int i = 0; i < vertexCount; i++) {
			float x = reader.getX(i);
			float y = reader.getY(i);
			float z = reader.getZ(i);

			float u = reader.getU(i);
			float v = reader.getV(i);

			float xN = reader.getNX(i);
			float yN = reader.getNY(i);
			float zN = reader.getNZ(i);

			writer.putVertex(x, y, z, xN, yN, zN, u, v);
		}
	}
}
