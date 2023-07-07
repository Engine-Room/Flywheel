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
	float getX(int index);

	float getY(int index);

	float getZ(int index);

	byte getR(int index);

	byte getG(int index);

	byte getB(int index);

	byte getA(int index);

	float getU(int index);

	float getV(int index);

	int getLight(int index);

	float getNX(int index);

	float getNY(int index);

	float getNZ(int index);

	int getVertexCount();

	default boolean isEmpty() {
		return getVertexCount() == 0;
	}

	default void delete() {
	}
}
