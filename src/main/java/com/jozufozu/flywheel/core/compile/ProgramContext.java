package com.jozufozu.flywheel.core.compile;

import java.util.Objects;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.CoreShaderInfoMap.CoreShaderInfo.FogType;
import com.jozufozu.flywheel.core.GameStateRegistry;
import com.jozufozu.flywheel.core.shader.ProgramSpec;
import com.jozufozu.flywheel.core.shader.StateSnapshot;

import net.minecraft.resources.ResourceLocation;

/**
 * Represents the entire context of a program's usage.
 */
public final class ProgramContext {
	/**
	 * Creates a compilation context for the given program, vertex type and render layer.
	 *
	 * @param programName 	The name of the program to use.
	 * @param vertexType  	The vertex type to use.
	 * @param alphaDiscard	The alpha threshold below which pixels are discarded.
	 * @return A compilation context.
	 */
	public static ProgramContext create(ResourceLocation programName, VertexType vertexType, float alphaDiscard, FogType fogType) {
		ProgramSpec spec = Backend.getSpec(programName);

		if (spec == null) {
			throw new NullPointerException("Cannot compile shader because '" + programName + "' is not recognized.");
		}

		return new ProgramContext(spec, alphaDiscard, fogType, vertexType, GameStateRegistry.takeSnapshot());
	}

	public final ProgramSpec spec;
	public final float alphaDiscard;
	public final FogType fogType;
	public final VertexType vertexType;
	public final StateSnapshot ctx;

	/**
	 * @param spec			The program to use.
	 * @param alphaDiscard 	Alpha threshold below which pixels are discarded.
	 * @param fogType       Which type of fog should be applied.
	 * @param vertexType   	The vertexType the program should be adapted for.
	 * @param ctx          	A snapshot of the game state.
	 */
	public ProgramContext(ProgramSpec spec, float alphaDiscard, FogType fogType, VertexType vertexType, StateSnapshot ctx) {
		this.spec = spec;
		this.alphaDiscard = alphaDiscard;
		this.fogType = fogType;
		this.vertexType = vertexType;
		this.ctx = ctx;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		var that = (ProgramContext) o;
		return spec == that.spec && vertexType == that.vertexType && ctx.equals(that.ctx) && Float.floatToIntBits(alphaDiscard) == Float.floatToIntBits(that.alphaDiscard) && fogType == that.fogType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(spec, alphaDiscard, fogType, vertexType, ctx);
	}

	@Override
	public String toString() {
		return "ProgramContext{" + "spec=" + spec + ", alphaDiscard=" + alphaDiscard + ", fogType=" + fogType + ", vertexType=" + vertexType + ", ctx=" + ctx + '}';
	}
}
