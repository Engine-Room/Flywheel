package com.jozufozu.flywheel.core.compile;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.RenderLayer;
import com.jozufozu.flywheel.core.shader.ProgramSpec;

import net.minecraft.resources.ResourceLocation;

/**
 * Represents the entire context of a program's usage.
 *
 * @param alphaDiscard Alpha threshold below which pixels are discarded.
 * @param vertexType   The vertexType the program should be adapted for.
 * @param spec         The generic program name.
 * @param ctx          An ID representing the state at the time of usage.
 */
public record ProgramContext(float alphaDiscard, VertexType vertexType, ProgramSpec spec, long ctx) {

	/**
	 * Creates a compilation context for the given program, vertex type and render layer.
	 *
	 * @param programName The name of the program to use.
	 * @param vertexType  The vertex type to use.
	 * @param layer       If cutout, the alpha discard threshold is 0.1, otherwise 0.
	 * @return A compilation context.
	 */
	public static ProgramContext create(ResourceLocation programName, VertexType vertexType, @Nullable RenderLayer layer) {
		ProgramSpec spec = Backend.getInstance()
				.getSpec(programName);

		if (spec == null) {
			throw new NullPointerException("Cannot compile shader because '" + programName + "' is not recognized.");
		}

		return new ProgramContext(getAlphaDiscard(layer), vertexType, spec, spec.getCurrentStateID());
	}

	/**
	 * Gets the alpha discard threshold for the given render layer.
	 *
	 * @param layer The render layer to get the alpha discard threshold for.
	 * @return The alpha discard threshold.
	 */
	public static float getAlphaDiscard(@Nullable RenderLayer layer) {
		return layer == RenderLayer.CUTOUT ? 0.1f : 0f;
	}

	public List<String> createDefines() {
		return spec().getDefines(ctx());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProgramContext that = (ProgramContext) o;
		// override for instance equality on vertexType
		return alphaDiscard == that.alphaDiscard && ctx == that.ctx && vertexType == that.vertexType && spec.equals(that.spec);
	}

	@Override
	public int hashCode() {
		return Objects.hash(alphaDiscard, vertexType, spec, ctx);
	}
}
