package com.jozufozu.flywheel.backend.model;

import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.mojang.blaze3d.vertex.VertexFormat;

public class ElementBuffer {

	protected final int elementCount;
	protected final VertexFormat.IndexType eboIndexType;
	private final int glBuffer;

	public ElementBuffer(int backing, int elementCount, VertexFormat.IndexType indexType) {
		this.elementCount = elementCount;
		this.eboIndexType = indexType;
		this.glBuffer = backing;
	}

	public void bind() {
		GlBufferType.ELEMENT_ARRAY_BUFFER.bind(glBuffer);
	}

	public int getElementCount() {
		return elementCount;
	}

	public VertexFormat.IndexType getEboIndexType() {
		return eboIndexType;
	}
}
