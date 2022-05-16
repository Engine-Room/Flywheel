package com.jozufozu.flywheel.core.compile;

import java.util.Objects;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.CoreShaderInfoMap.CoreShaderInfo.FogType;
import com.jozufozu.flywheel.core.shader.StateSnapshot;
import com.jozufozu.flywheel.core.source.FileResolution;

/**
 * Represents the entire context of a program's usage.
 */
public final class ProgramContext {

	public final VertexType vertexType;
	public final FileResolution instanceShader;
	public final FileResolution vertexMaterialShader;
	public final FileResolution fragmentMaterialShader;
	public final float alphaDiscard;
	public final FogType fogType;
	public final StateSnapshot ctx;

	/**
	 * @param vertexType              The vertexType the program should be adapted for.
	 * @param instanceShader          The instance shader to use.
	 * @param vertexMaterialShader    The vertex material shader to use.
	 * @param fragmentMaterialShader  The fragment material shader to use.
	 * @param alphaDiscard            Alpha threshold below which pixels are discarded.
	 * @param fogType                 Which type of fog should be applied.
	 * @param ctx                     A snapshot of the game state.
	 */
	public ProgramContext(VertexType vertexType, FileResolution instanceShader, FileResolution vertexMaterialShader, FileResolution fragmentMaterialShader, float alphaDiscard, FogType fogType, StateSnapshot ctx) {
		this.vertexType = vertexType;
		this.instanceShader = instanceShader;
		this.vertexMaterialShader = vertexMaterialShader;
		this.fragmentMaterialShader = fragmentMaterialShader;
		this.alphaDiscard = alphaDiscard;
		this.fogType = fogType;
		this.ctx = ctx;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		var that = (ProgramContext) o;
		return vertexType == that.vertexType && instanceShader == that.instanceShader && vertexMaterialShader == that.vertexMaterialShader && fragmentMaterialShader == that.fragmentMaterialShader && ctx.equals(that.ctx) && Float.floatToIntBits(alphaDiscard) == Float.floatToIntBits(that.alphaDiscard) && fogType == that.fogType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(vertexType, instanceShader, vertexMaterialShader, fragmentMaterialShader, alphaDiscard, fogType, ctx);
	}

	@Override
	public String toString() {
		return "ProgramContext{" + "vertexType=" + vertexType + ", instanceShader=" + instanceShader + ", vertexMaterialShader=" + vertexMaterialShader + ", fragmentMaterialShader=" + fragmentMaterialShader + ", alphaDiscard=" + alphaDiscard + ", fogType=" + fogType + ", ctx=" + ctx + '}';
	}
}
