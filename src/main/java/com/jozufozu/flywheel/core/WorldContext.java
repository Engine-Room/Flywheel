package com.jozufozu.flywheel.core;

import java.util.function.Supplier;
import java.util.stream.Stream;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.ShaderContext;
import com.jozufozu.flywheel.backend.material.MaterialSpec;
import com.jozufozu.flywheel.backend.pipeline.IShaderPipeline;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;

import net.minecraft.util.ResourceLocation;

public class WorldContext<P extends WorldProgram> extends ShaderContext<P> {
	protected ResourceLocation name;
	protected Supplier<Stream<ResourceLocation>> specStream;

	public final IShaderPipeline<P> pipeline;

	public WorldContext(Backend backend, IShaderPipeline<P> factory) {
		super(backend);
		this.pipeline = factory;

		specStream = () -> backend.allMaterials()
				.stream()
				.map(MaterialSpec::getProgramName);
	}

	public WorldContext<P> withName(ResourceLocation name) {
		this.name = name;
		return this;
	}

	public WorldContext<P> withSpecStream(Supplier<Stream<ResourceLocation>> specStream) {
		this.specStream = specStream;
		return this;
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
}
