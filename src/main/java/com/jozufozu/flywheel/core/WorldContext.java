package com.jozufozu.flywheel.core;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.IShaderContext;
import com.jozufozu.flywheel.backend.material.MaterialSpec;
import com.jozufozu.flywheel.backend.pipeline.IShaderPipeline;
import com.jozufozu.flywheel.core.shader.IMultiProgram;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;

import net.minecraft.util.ResourceLocation;

public class WorldContext<P extends WorldProgram> implements IShaderContext<P> {
	public final Backend backend;
	protected final Map<ResourceLocation, IMultiProgram<P>> programs = new HashMap<>();
	protected final ResourceLocation name;
	protected final Supplier<Stream<ResourceLocation>> specStream;

	public final IShaderPipeline<P> pipeline;

	public WorldContext(Backend backend, ResourceLocation name, Supplier<Stream<ResourceLocation>> specStream, IShaderPipeline<P> pipeline) {
		this.backend = backend;
		this.name = name;
		this.specStream = specStream;
		this.pipeline = pipeline;
	}

	@Override
	public void load() {

		Backend.log.info("Loading context '{}'", name);

		specStream.get()
				.map(backend::getSpec)
				.forEach(this::loadSpec);
	}

	private void loadSpec(ProgramSpec spec) {

		try {
			programs.put(spec.name, pipeline.compile(spec));

			Backend.log.debug("Loaded program {}", spec.name);
		} catch (Exception e) {
			Backend.log.error("Error loading program {}", spec.name);
			Backend.log.error("", e);
			backend.sources.notifyError();
		}
	}

	@Override
	public Supplier<P> getProgramSupplier(ResourceLocation spec) {
		return programs.get(spec);
	}

	@Override
	public void delete() {
		programs.values()
				.forEach(IMultiProgram::delete);
		programs.clear();
	}

	public static Builder builder(Backend backend, ResourceLocation name) {
		return new Builder(backend, name);
	}

	public static class Builder {
		private final Backend backend;
		private final ResourceLocation name;
		private Supplier<Stream<ResourceLocation>> specStream;

		public Builder(Backend backend, ResourceLocation name) {
			this.backend = backend;
			this.name = name;
		}

		public Builder setSpecStream(Supplier<Stream<ResourceLocation>> specStream) {
			this.specStream = specStream;
			return this;
		}

		public <P extends WorldProgram> WorldContext<P> build(IShaderPipeline<P> pipeline) {
			if (specStream == null) {
				specStream = () -> backend.allMaterials()
						.stream()
						.map(MaterialSpec::getProgramName);
			}
			return new WorldContext<>(backend, name, specStream, pipeline);
		}
	}
}
