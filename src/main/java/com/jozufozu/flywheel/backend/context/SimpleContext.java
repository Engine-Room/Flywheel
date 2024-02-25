package com.jozufozu.flywheel.backend.context;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.backend.engine.textures.TextureSource;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

public class SimpleContext implements Context {
	private final ContextShader contextShader;
	private final Preparation preparation;

	public SimpleContext(ContextShader contextShader, Preparation preparation) {
		this.contextShader = contextShader;
		this.preparation = preparation;
	}

	public static Builder builder(ContextShader contextShader) {
		return new Builder(contextShader);
	}

	@Override
	public ContextShader contextShader() {
		return contextShader;
	}

	@Override
	public void prepare(Material material, GlProgram shader, TextureSource textureSource) {
		preparation.prepare(material, shader, textureSource);
	}

	@FunctionalInterface
	public interface Preparation {
		void prepare(Material material, GlProgram shader, TextureSource textureSource);
	}

	public static class Builder {
		private final ContextShader contextShader;
		@Nullable
		private Preparation preparation;

		public Builder(ContextShader contextShader) {
			this.contextShader = contextShader;
		}

		public Builder preparation(Preparation preparation) {
			this.preparation = preparation;
			return this;
		}

		public SimpleContext build() {
			if (preparation == null) {
				preparation = (material, shader, textureSource) -> {
				};
			}
			return new SimpleContext(contextShader, preparation);
		}
	}
}
