package com.jozufozu.flywheel.backend.compile.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.compile.SourceLoader;
import com.jozufozu.flywheel.glsl.SourceComponent;
import com.jozufozu.flywheel.glsl.SourceFile;
import com.jozufozu.flywheel.glsl.generate.GlslBuilder;

import net.minecraft.resources.ResourceLocation;

public class UniformComponent implements SourceComponent {
	private final ResourceLocation name;
	private final ImmutableList<SourceFile> uniformShaders;

	public static Builder builder(ResourceLocation uniforms) {
		return new Builder(uniforms);
	}

	private UniformComponent(ResourceLocation name, ImmutableList<SourceFile> uniformShaders) {
		this.name = name;
		this.uniformShaders = uniformShaders;
	}

	@Override
	public Collection<? extends SourceComponent> included() {
		return uniformShaders;
	}

	@Override
	public String source() {
		var builder = new GlslBuilder();

		builder.uniformBlock()
			.layout("std140")
			.name("FlwUniforms")
			.member("FlywheelUniforms", "flywheel");

		builder.blankLine();

		return builder.build();
	}

	@Override
	public ResourceLocation name() {
		return name;
	}

	public static class Builder {
		private final ResourceLocation name;
		private final List<ResourceLocation> uniformShaders = new ArrayList<>();

		public Builder(ResourceLocation name) {
			this.name = name;
		}

		public Builder sources(List<ResourceLocation> sources) {
			this.uniformShaders.addAll(sources);
			return this;
		}

		public UniformComponent build(SourceLoader sources) {
			var out = ImmutableList.<SourceFile>builder();

			boolean errored = false;
			for (ResourceLocation uniformShader : uniformShaders) {
				SourceFile sourceFile = sources.find(uniformShader);
				if (sourceFile != null) {
					out.add(sourceFile);
				} else {
					errored = true;
				}
			}

			if (errored) {
				return null;
			}

			return new UniformComponent(name, out.build());
		}
	}
}
