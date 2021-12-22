package com.jozufozu.flywheel.core.vertex;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.util.ModelReader;

public class PosNormalTexType implements VertexType {

	public static final PosNormalTexType INSTANCE = new PosNormalTexType();

	@Override
	public VertexFormat getFormat() {
		return Formats.UNLIT_MODEL;
	}

	@Override
	public void copyInto(ByteBuffer buffer, ModelReader reader) {
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
