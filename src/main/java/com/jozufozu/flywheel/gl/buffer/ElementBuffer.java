package com.jozufozu.flywheel.gl.buffer;

import com.mojang.blaze3d.vertex.VertexFormat;

public class ElementBuffer {
	protected final int elementCount;
	protected final VertexFormat.IndexType eboIndexType;
	public final int glBuffer;

	public ElementBuffer(int backing, int elementCount, VertexFormat.IndexType indexType) {
		this.elementCount = elementCount;
		this.eboIndexType = indexType;
		this.glBuffer = backing;
	}

	public int getElementCount() {
		return elementCount;
	}

	public VertexFormat.IndexType getEboIndexType() {
		return eboIndexType;
	}
}
