package com.jozufozu.flywheel.backend.compile;

import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.glsl.GLSLVersion;
import com.jozufozu.flywheel.glsl.SourceComponent;

import net.minecraft.resources.ResourceLocation;

// TODO: move shader api redefinition to a separate file?
public record Pipeline(GLSLVersion glslVersion, ResourceLocation vertexShader, ResourceLocation fragmentShader, InstanceAssembler assembler) {
	@FunctionalInterface
	public interface InstanceAssembler {
		/**
		 * Generate the source component necessary to convert a packed {@link InstanceType} into its shader representation.
		 *
		 * @return A source component defining functions that unpack a representation of the given instance type.
		 */
		SourceComponent assemble(InstanceAssemblerContext context);
	}

	public record InstanceAssemblerContext(VertexType vertexType, InstanceType<?> instanceType) {
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private GLSLVersion glslVersion;
		private ResourceLocation vertex;
		private ResourceLocation fragment;
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

		public Builder assembler(InstanceAssembler assembler) {
			this.assembler = assembler;
			return this;
		}

		public Pipeline build() {
			return new Pipeline(glslVersion, vertex, fragment, assembler);
		}
	}
}
