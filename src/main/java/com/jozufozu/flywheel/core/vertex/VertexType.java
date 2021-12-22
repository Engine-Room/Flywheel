package com.jozufozu.flywheel.core.vertex;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;

public interface VertexType {

	VertexFormat getFormat();

    void copyInto(ByteBuffer buffer, VertexList reader);

	default int getStride() {
		return getFormat().getStride();
	}
}
