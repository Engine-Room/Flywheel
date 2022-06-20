package com.jozufozu.flywheel.backend.model;

import com.jozufozu.flywheel.backend.gl.GlNumericType;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;

public class ElementBuffer {

	public final GlBuffer buffer;
	public final int elementCount;
	public final GlNumericType eboIndexType;

	public ElementBuffer(GlBuffer backing, int elementCount, GlNumericType indexType) {
		this.buffer = backing;
		this.eboIndexType = indexType;
		this.elementCount = elementCount;
	}
}
