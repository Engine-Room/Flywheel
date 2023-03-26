package com.jozufozu.flywheel.api.vertex;

public interface MutableVertexList extends VertexList {
	void x(int index, float x);

	void y(int index, float y);

	void z(int index, float z);

	void r(int index, float r);

	void g(int index, float g);

	void b(int index, float b);

	void a(int index, float a);

	void u(int index, float u);

	void v(int index, float v);

	void overlay(int index, int overlay);

	void light(int index, int light);

	void normalX(int index, float normalX);

	void normalY(int index, float normalY);

	void normalZ(int index, float normalZ);
}
