package com.jozufozu.flywheel.core.vertex;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.util.ModelReader;

public interface VertexType {

	VertexFormat getFormat();

    void copyInto(ByteBuffer buffer, ModelReader reader);

	default int getStride() {
		return getFormat().getStride();
	}
}
