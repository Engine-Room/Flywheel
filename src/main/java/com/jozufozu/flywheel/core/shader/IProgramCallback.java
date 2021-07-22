package com.jozufozu.flywheel.core.shader;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

/**
 * Used to define shader uniforms.
 */
@FunctionalInterface
public interface IProgramCallback<P extends GlProgram> {

	void call(P program);

	default IProgramCallback<P> andThen(IProgramCallback<P> other) {
		if (other == null) return this;

		return program -> {
			call(program);
			other.call(program);
		};
	}
}
