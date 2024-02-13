package com.jozufozu.flywheel.lib.context;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.context.ContextShader;
import com.jozufozu.flywheel.api.context.Shader;
import com.jozufozu.flywheel.api.context.TextureSource;
import com.jozufozu.flywheel.api.material.Material;

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
	public void prepare(Material material, Shader shader, TextureSource textureSource) {
		preparation.prepare(material, shader, textureSource);
	}

	@FunctionalInterface
	public interface Preparation {
		void prepare(Material material, Shader shader, TextureSource textureSource);
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
