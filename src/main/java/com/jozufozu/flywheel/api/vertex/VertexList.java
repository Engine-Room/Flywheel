package com.jozufozu.flywheel.api.vertex;

/**
 * A read only view of a vertex buffer.
 *
 * <p>
 *     VertexList assumes nothing about the layout of the vertices. Implementations should feel free to return constants
 *     for values that are unused in their layout.
 * </p>
 * TODO: more flexible elements?
 */
public interface VertexList {
	float x(int index);

	float y(int index);

	float z(int index);

	byte r(int index);

	byte g(int index);

	byte b(int index);

	byte a(int index);

	default int color(int index) {
		return a(index) << 24 | r(index) << 16 | g(index) << 8 | b(index);
	}

	float u(int index);

	float v(int index);

	int overlay(int index);

	int light(int index);

	float normalX(int index);

	float normalY(int index);

	float normalZ(int index);

	int getVertexCount();

	default boolean isEmpty() {
		return getVertexCount() == 0;
	}
}
