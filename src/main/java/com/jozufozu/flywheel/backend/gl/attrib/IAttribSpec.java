package com.jozufozu.flywheel.backend.gl.attrib;

public interface IAttribSpec {

	void vertexAttribPointer(int stride, int index, int offset);

	int getSize();

	int getAttributeCount();
}
