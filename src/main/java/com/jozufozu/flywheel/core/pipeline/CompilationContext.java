package com.jozufozu.flywheel.core.pipeline;

import java.util.Objects;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.source.SourceFile;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;

/**
 * Represents the entire context of a program's usage.
 *
 * @param vertexType The vertexType the program should be adapted for.
 * @param spec The generic program name.
 * @param ctx An ID representing the state at the time of usage.
 */
public record CompilationContext(VertexType vertexType, ProgramSpec spec, long ctx) {

	public SourceFile getFile() {
		return spec().getSource();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CompilationContext that = (CompilationContext) o;
		// override for instance equality on vertexType
		return ctx == that.ctx && vertexType == that.vertexType && spec.equals(that.spec);
	}

	@Override
	public int hashCode() {
		return Objects.hash(vertexType, spec, ctx);
	}
}
