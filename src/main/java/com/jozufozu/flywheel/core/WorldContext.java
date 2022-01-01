package com.jozufozu.flywheel.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.jozufozu.flywheel.api.struct.Instanced;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.RenderLayer;
import com.jozufozu.flywheel.backend.ShaderContext;
import com.jozufozu.flywheel.backend.source.ShaderLoadingException;
import com.jozufozu.flywheel.core.pipeline.CachingCompiler;
import com.jozufozu.flywheel.core.pipeline.CompilationContext;
import com.jozufozu.flywheel.core.pipeline.PipelineCompiler;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;

import net.minecraft.resources.ResourceLocation;

public class WorldContext<P extends WorldProgram> implements ShaderContext<P> {
	public final Backend backend;
	protected final Map<ResourceLocation, ProgramSpec> programs = new HashMap<>();
	protected final ResourceLocation name;
	protected final Supplier<Stream<ResourceLocation>> specStream;
	private final CachingCompiler<P> programCache;

	public WorldContext(Backend backend, ResourceLocation name, Supplier<Stream<ResourceLocation>> specStream, PipelineCompiler<P> pipeline) {
		this.backend = backend;
		this.name = name;
		this.specStream = specStream;
		this.programCache = new CachingCompiler<>(pipeline);
	}

	@Override
	public void load() {

		Backend.LOGGER.info("Loading context '{}'", name);

		specStream.get()
				.map(backend::getSpec)
				.forEach(this::loadSpec);
	}

	private void loadSpec(ProgramSpec spec) {

		try {
			programs.put(spec.name, spec);

			Backend.LOGGER.debug("Loaded program {}", spec.name);
		} catch (Exception e) {
			Backend.LOGGER.error("Error loading program {}", spec.name);
			if (!(e instanceof ShaderLoadingException)) {
				Backend.LOGGER.error("", e);
			}
			backend.loader.notifyError();
		}
	}

	@Override
	public P getProgram(ResourceLocation loc, VertexType vertexType, RenderLayer layer) {
		ProgramSpec spec = programs.get(loc);

		if (spec == null) {
			throw new NullPointerException("Cannot compile shader because '" + loc + "' is not recognized.");
		}

		return programCache.getProgram(CompilationContext.create(vertexType, layer, spec));
	}

	@Override
	public void delete() {
		programCache.invalidate();
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

		public <P extends WorldProgram> WorldContext<P> build(PipelineCompiler<P> pipeline) {
			if (specStream == null) {
				specStream = () -> backend.allMaterials()
						.stream()
						.map(t -> t instanceof Instanced<?> i ? i : null)
						.filter(Objects::nonNull)
						.map(Instanced::getProgramSpec);
			}
			return new WorldContext<>(backend, name, specStream, pipeline);
		}
	}
}
