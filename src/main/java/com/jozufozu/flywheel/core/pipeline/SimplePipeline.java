package com.jozufozu.flywheel.core.pipeline;

import com.jozufozu.flywheel.api.pipeline.Pipeline;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.core.SourceComponent;
import com.jozufozu.flywheel.core.source.FileResolution;

public final class SimplePipeline implements Pipeline {
	private final GLSLVersion glslVersion;
	private final FileResolution vertex;
	private final FileResolution fragment;
	private final InstanceAssemblerFactory factory;

	public SimplePipeline(GLSLVersion glslVersion, FileResolution vertex, FileResolution fragment, InstanceAssemblerFactory factory) {
		this.glslVersion = glslVersion;
		this.vertex = vertex;
		this.fragment = fragment;
		this.factory = factory;
	}

	/**
	 * Generate the source component necessary to convert a packed {@link StructType} into its shader representation.
	 *
	 * @return A source component defining functions that unpack a representation of the given struct type.
	 */
	@Override
	public SourceComponent assemble(InstanceAssemblerContext context) {
		return factory.apply(context);
	}

	@Override
	public GLSLVersion glslVersion() {
		return glslVersion;
	}

	@Override
	public FileResolution vertex() {
		return vertex;
	}

	@Override
	public FileResolution fragment() {
		return fragment;
	}

	@FunctionalInterface
	public interface InstanceAssemblerFactory {
		SourceComponent apply(InstanceAssemblerContext context);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private GLSLVersion glslVersion;
		private FileResolution vertex;
		private FileResolution fragment;
		private InstanceAssemblerFactory factory;

		public Builder glslVersion(GLSLVersion glslVersion) {
			this.glslVersion = glslVersion;
			return this;
		}

		public Builder vertex(FileResolution vertex) {
			this.vertex = vertex;
			return this;
		}

		public Builder fragment(FileResolution fragment) {
			this.fragment = fragment;
			return this;
		}

		public Builder assemblerFactory(InstanceAssemblerFactory factory) {
			this.factory = factory;
			return this;
		}

		public SimplePipeline build() {
			return new SimplePipeline(glslVersion, vertex, fragment, factory);
		}
	}
}
