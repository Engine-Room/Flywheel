package com.jozufozu.flywheel.core.layout;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.gl.GlNumericType;

public class PrimitiveItem implements LayoutItem {

	private final GlNumericType type;
	private final int count;
	private final int size;
	private final int attributeCount;
	private final boolean normalized;

	public PrimitiveItem(GlNumericType type, int count) {
		this(type, count, false);
	}

	public PrimitiveItem(GlNumericType type, int count, boolean normalized) {
		this.type = type;
		this.count = count;
		this.size = type.getByteWidth() * count;
		this.attributeCount = (this.size + 15) / 16; // ceiling division. GLSL vertex attributes can only be 16 bytes wide
		this.normalized = normalized;
	}

	@Override
	public void vertexAttribPointer(int stride, int index, int offset) {
		GL20.glVertexAttribPointer(index, count, type.getGlEnum(), normalized, stride, offset);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public int attributeCount() {
		return attributeCount;
	}

}
