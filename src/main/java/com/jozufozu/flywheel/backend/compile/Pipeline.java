package com.jozufozu.flywheel.backend.compile;

import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.glsl.GlslVersion;
import com.jozufozu.flywheel.backend.glsl.SourceComponent;

import net.minecraft.resources.ResourceLocation;

public record Pipeline(GlslVersion glslVersion, ResourceLocation vertexMain, ResourceLocation fragmentMain,
					   InstanceAssembler assembler, String compilerMarker, Consumer<GlProgram> onLink) {

	@FunctionalInterface
	public interface InstanceAssembler {
		/**
		 * Generate the source component necessary to convert a packed {@link Instance} into its shader representation.
		 *
		 * @return A source component defining functions that unpack a representation of the given instance type.
		 */
		SourceComponent assemble(InstanceType<?> instanceType);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		@Nullable
		private GlslVersion glslVersion;
		@Nullable
		private ResourceLocation vertexMain;
		@Nullable
		private ResourceLocation fragmentMain;
		@Nullable
		private InstanceAssembler assembler;
		@Nullable
		private String compilerMarker;
		@Nullable
		private Consumer<GlProgram> onLink;

		public Builder glslVersion(GlslVersion glslVersion) {
			this.glslVersion = glslVersion;
			return this;
		}

		public Builder vertexMain(ResourceLocation shader) {
			this.vertexMain = shader;
			return this;
		}

		public Builder fragmentMain(ResourceLocation shader) {
			this.fragmentMain = shader;
			return this;
		}

		public Builder assembler(InstanceAssembler assembler) {
			this.assembler = assembler;
			return this;
		}

		public Builder compilerMarker(String compilerMarker) {
			this.compilerMarker = compilerMarker;
			return this;
		}

		public Builder onLink(Consumer<GlProgram> onLink) {
			this.onLink = onLink;
			return this;
		}

		public Pipeline build() {
			Objects.requireNonNull(glslVersion);
			Objects.requireNonNull(vertexMain);
			Objects.requireNonNull(fragmentMain);
			Objects.requireNonNull(assembler);
			Objects.requireNonNull(compilerMarker);
			Objects.requireNonNull(onLink);
			return new Pipeline(glslVersion, vertexMain, fragmentMain, assembler, compilerMarker, onLink);
		}
	}
}
