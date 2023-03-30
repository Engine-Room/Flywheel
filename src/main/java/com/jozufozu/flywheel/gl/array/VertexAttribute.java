package com.jozufozu.flywheel.gl.array;

public interface VertexAttribute {
	int getByteWidth();

	/**
	 * Apply this vertex attribute to the bound vertex array.
	 * @param offset The byte offset to the first element of the attribute.
	 * @param i The attribute index.
	 * @param stride The byte stride between consecutive elements of the attribute.
	 */
	void pointer(long offset, int i, int stride);

	/**
	 * Use DSA to apply this vertex attribute to the given vertex array.
	 * @param vaobj The vertex array object to modify.
	 * @param i The attribute index.
	 */
	void format(int vaobj, int i);
}
