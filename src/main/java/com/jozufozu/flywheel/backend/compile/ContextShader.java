package com.jozufozu.flywheel.backend.compile;

import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

import net.minecraft.resources.ResourceLocation;

public record ContextShader(ResourceLocation vertexShader, ResourceLocation fragmentShader,
							Consumer<GlProgram> onLink) {
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		@Nullable
		private ResourceLocation vertexShader;
		@Nullable
		private ResourceLocation fragmentShader;
		@Nullable
		private Consumer<GlProgram> onLink;

		public Builder vertexShader(ResourceLocation shader) {
			this.vertexShader = shader;
			return this;
		}

		public Builder fragmentShader(ResourceLocation shader) {
			this.fragmentShader = shader;
			return this;
		}

		public Builder onLink(Consumer<GlProgram> onLink) {
			this.onLink = onLink;
			return this;
		}

		public ContextShader build() {
			Objects.requireNonNull(vertexShader);
			Objects.requireNonNull(fragmentShader);
			Objects.requireNonNull(onLink);
			return new ContextShader(vertexShader, fragmentShader, onLink);
		}
	}
}
