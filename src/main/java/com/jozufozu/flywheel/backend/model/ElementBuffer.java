package com.jozufozu.flywheel.backend.model;

import com.jozufozu.flywheel.backend.gl.GlNumericType;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;

public class ElementBuffer {

	private final GlBuffer buffer;
	public final int elementCount;
	public final VertexFormat.IndexType eboIndexType;

	public ElementBuffer(GlBuffer backing, int elementCount, VertexFormat.IndexType indexType) {
		this.buffer = backing;
		this.eboIndexType = indexType;
		this.elementCount = elementCount;
	}

	public void bind() {
		buffer.bind();
	}

	public void unbind() {
		buffer.unbind();
	}
}
