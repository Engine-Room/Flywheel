package com.jozufozu.flywheel.api.vertex;

public interface MutableVertexList extends VertexList {
	void x(int index, float x);

	void y(int index, float y);

	void z(int index, float z);

	void r(int index, byte r);

	void g(int index, byte g);

	void b(int index, byte b);

	void a(int index, byte a);

	default void color(int index, int color) {
		a(index, (byte) (color >> 24 & 0xFF));
		r(index, (byte) (color >> 16 & 0xFF));
		g(index, (byte) (color >> 8 & 0xFF));
		b(index, (byte) (color & 0xFF));
	}

	void u(int index, float u);

	void v(int index, float v);

	void overlay(int index, int overlay);

	void light(int index, int light);

	void normalX(int index, float normalX);

	void normalY(int index, float normalY);

	void normalZ(int index, float normalZ);
}
