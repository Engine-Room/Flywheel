package com.jozufozu.flywheel.core.layout;

record PaddingItem(int bytes) implements LayoutItem {

	@Override
	public void vertexAttribPointer(int stride, int index, int offset) {

	}

	@Override
	public int getSize() {
		return bytes;
	}

	@Override
	public int getAttributeCount() {
		return 0;
	}
}
