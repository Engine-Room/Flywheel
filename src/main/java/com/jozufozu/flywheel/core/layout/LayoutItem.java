package com.jozufozu.flywheel.core.layout;

public interface LayoutItem {

	void vertexAttribPointer(int stride, int index, int offset);

	int size();

	int attributeCount();
}
