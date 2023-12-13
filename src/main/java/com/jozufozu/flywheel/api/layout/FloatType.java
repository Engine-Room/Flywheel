package com.jozufozu.flywheel.api.layout;

/**
 * The backing type of traditional floating point vertex attributes.
 * <br>
 * Integral types are implicitly converted to floats in the shader.
 */
public enum FloatType {
	BYTE,
	UNSIGNED_BYTE,
	SHORT,
	UNSIGNED_SHORT,
	INT,
	UNSIGNED_INT,
	HALF_FLOAT,
	FLOAT,
	DOUBLE,
	FIXED,
}
