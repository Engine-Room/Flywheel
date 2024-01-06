package com.jozufozu.flywheel.backend.compile;

import java.util.Objects;

import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.backend.glsl.GlslVersion;
import com.jozufozu.flywheel.backend.glsl.SourceComponent;

import net.minecraft.resources.ResourceLocation;

public record Pipeline(GlslVersion glslVersion, ResourceLocation vertexMain, ResourceLocation fragmentMain,
					   ResourceLocation vertexApiImpl, ResourceLocation fragmentApiImpl, InstanceAssembler assembler) {
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
		private GlslVersion glslVersion;
		private ResourceLocation vertexMain;
		private ResourceLocation fragmentMain;
		private ResourceLocation vertexApiImpl;
		private ResourceLocation fragmentApiImpl;
		private InstanceAssembler assembler;

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

		public Builder vertexApiImpl(ResourceLocation shader) {
			this.vertexApiImpl = shader;
			return this;
		}

		public Builder fragmentApiImpl(ResourceLocation shader) {
			this.fragmentApiImpl = shader;
			return this;
		}

		public Builder assembler(InstanceAssembler assembler) {
			this.assembler = assembler;
			return this;
		}

		public Pipeline build() {
			Objects.requireNonNull(glslVersion);
			Objects.requireNonNull(vertexMain);
			Objects.requireNonNull(fragmentMain);
			Objects.requireNonNull(vertexApiImpl);
			Objects.requireNonNull(fragmentApiImpl);
			Objects.requireNonNull(assembler);
			return new Pipeline(glslVersion, vertexMain, fragmentMain, vertexApiImpl, fragmentApiImpl, assembler);
		}
	}
}
