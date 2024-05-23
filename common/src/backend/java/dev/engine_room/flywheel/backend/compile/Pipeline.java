package dev.engine_room.flywheel.backend.compile;

import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import dev.engine_room.flywheel.backend.glsl.SourceComponent;
import net.minecraft.resources.ResourceLocation;

public record Pipeline(ResourceLocation vertexMain, ResourceLocation fragmentMain,
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
		private ResourceLocation vertexMain;
		@Nullable
		private ResourceLocation fragmentMain;
		@Nullable
		private InstanceAssembler assembler;
		@Nullable
		private String compilerMarker;
		@Nullable
		private Consumer<GlProgram> onLink;

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
			Objects.requireNonNull(vertexMain);
			Objects.requireNonNull(fragmentMain);
			Objects.requireNonNull(assembler);
			Objects.requireNonNull(compilerMarker);
			Objects.requireNonNull(onLink);
			return new Pipeline(vertexMain, fragmentMain, assembler, compilerMarker, onLink);
		}
	}
}
