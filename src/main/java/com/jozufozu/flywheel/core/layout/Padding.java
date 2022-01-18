package com.jozufozu.flywheel.core.layout;

record Padding(int bytes) implements LayoutItem {

	@Override
	public void vertexAttribPointer(int stride, int index, int offset) {

	}

	@Override
	public int size() {
		return bytes;
	}

	@Override
	public int attributeCount() {
		return 0;
	}

}
