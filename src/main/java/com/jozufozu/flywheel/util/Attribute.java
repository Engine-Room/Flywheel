package com.jozufozu.flywheel.util;

import java.nio.FloatBuffer;

public interface Attribute {

	/**
	 * Append the contents of this object to the given FloatBuffer.
	 */
	void append(FloatBuffer buffer);
}
