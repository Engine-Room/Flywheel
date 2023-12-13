package com.jozufozu.flywheel.api.layout;

/**
 * A single element in a {@link Layout}.
 */
public sealed interface Element {
	String name();

	/**
	 * A simple vector of floats, i.e. {@code vec3}, {@code dvec2}.
	 * <br>
	 * If {@link #type} is an integral type, the shader will implicitly convert it to a float without normalization.
	 * If you want normalization, use {@link NormalizedVector}.
	 *
	 * @param name The name of the element to be used in the shader.
	 * @param type The backing type of the element.
	 * @param size The number of components in the vector.
	 */
	record Vector(String name, FloatType type, VectorSize size) implements Element {
	}

	/**
	 * A vector of integers, i.e. {@code ivec3}, {@code uvec2}.
	 * <br>
	 * All backing types will be presented as either {@code int} or {@code uint} in the shader,
	 * depending on the signedness of the type.
	 *
	 * @param name The name of the element to be used in the shader.
	 * @param type The backing type of the element.
	 * @param size The number of components in the vector.
	 */
	record IntegerVector(String name, IntegerType type, VectorSize size) implements Element {
	}

	/**
	 * A vector of integers, normalized and presented as a float in the shader.
	 *
	 * @param name The name of the element to be used in the shader.
	 * @param type The backing type of the element.
	 * @param size The number of components in the vector.
	 */
	record NormalizedVector(String name, IntegerType type, VectorSize size) implements Element {
	}

	/**
	 * A matrix of 32-bit floating point numbers, i.e. {@code mat3}, {@code mat2x4}.
	 *
	 * @param name The name of the element to be used in the shader.
	 * @param rows The number of rows in the matrix.
	 * @param cols The number of columns in the matrix.
	 */
	record Matrix(String name, MatrixSize rows, MatrixSize cols) implements Element {
	}
}
