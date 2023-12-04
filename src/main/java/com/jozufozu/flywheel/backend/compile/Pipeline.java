package com.jozufozu.flywheel.backend.compile;

import java.util.Objects;

import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.glsl.GLSLVersion;
import com.jozufozu.flywheel.glsl.SourceComponent;

import net.minecraft.resources.ResourceLocation;

public record Pipeline(GLSLVersion glslVersion, ResourceLocation vertexShader, ResourceLocation fragmentShader,
					   ResourceLocation vertexAPI, ResourceLocation fragmentAPI, InstanceAssembler assembler) {
	@FunctionalInterface
	public interface InstanceAssembler {
		/**
		 * Generate the source component necessary to convert a packed {@link InstanceType} into its shader representation.
		 *
		 * @return A source component defining functions that unpack a representation of the given instance type.
		 */
		SourceComponent assemble(InstanceAssemblerContext context);
	}

	public record InstanceAssemblerContext(int baseAttribute, InstanceType<?> instanceType) {
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private GLSLVersion glslVersion;
		private ResourceLocation vertex;
		private ResourceLocation fragment;
		private ResourceLocation vertexAPI;
		private ResourceLocation fragmentAPI;
		private InstanceAssembler assembler;

		public Builder glslVersion(GLSLVersion glslVersion) {
			this.glslVersion = glslVersion;
			return this;
		}

		public Builder vertex(ResourceLocation vertex) {
			this.vertex = vertex;
			return this;
		}

		public Builder fragment(ResourceLocation fragment) {
			this.fragment = fragment;
			return this;
		}

		public Builder vertexAPI(ResourceLocation vertex) {
			this.vertexAPI = vertex;
			return this;
		}

		public Builder fragmentAPI(ResourceLocation fragment) {
			this.fragmentAPI = fragment;
			return this;
		}

		public Builder assembler(InstanceAssembler assembler) {
			this.assembler = assembler;
			return this;
		}

		public Pipeline build() {
			Objects.requireNonNull(glslVersion);
			Objects.requireNonNull(vertex);
			Objects.requireNonNull(fragment);
			Objects.requireNonNull(vertexAPI);
			Objects.requireNonNull(fragmentAPI);
			Objects.requireNonNull(assembler);
			return new Pipeline(glslVersion, vertex, fragment, vertexAPI, fragmentAPI, assembler);
		}
	}
}
