package com.jozufozu.flywheel.backend.state;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.gl.GlTextureUnit;

import net.minecraft.util.ResourceLocation;

public class RenderState implements IRenderState {

	private final Map<GlTextureUnit, ResourceLocation> textures;
	private final ImmutableList<IRenderState> states;

	public RenderState(Map<GlTextureUnit, ResourceLocation> textures, ImmutableList<IRenderState> states) {
		this.textures = textures;
		this.states = states;
	}

	@Override
	public void bind() {
		states.forEach(IRenderState::bind);
	}

	@Override
	public void unbind() {
		states.forEach(IRenderState::unbind);
	}

	@Nullable
	@Override
	public ResourceLocation getTexture(GlTextureUnit textureUnit) {
		return textures.get(textureUnit);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RenderState that = (RenderState) o;
		return states.equals(that.states);
	}

	@Override
	public int hashCode() {
		return Objects.hash(states);
	}

	public static StateBuilder builder() {
		return new StateBuilder();
	}

	public static class StateBuilder {
		private final ImmutableList.Builder<IRenderState> states = ImmutableList.builder();
		private final Map<GlTextureUnit, ResourceLocation> textures = new EnumMap<>(GlTextureUnit.class);

		public StateBuilder texture(ResourceLocation name) {
			return addState(TextureRenderState.get(name));
		}

		public StateBuilder addState(IRenderState state) {
			if (state instanceof TextureRenderState) {
				TextureRenderState tex = (TextureRenderState) state;
				if (textures.put(tex.unit, tex.location) == null) {
					states.add(state);
				}
			} else {
				states.add(state);
			}
			return this;
		}

		public RenderState build() {
			return new RenderState(textures, states.build());
		}
	}
}
