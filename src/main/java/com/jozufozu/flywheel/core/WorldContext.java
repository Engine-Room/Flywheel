package com.jozufozu.flywheel.core;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.ResourceUtil;
import com.jozufozu.flywheel.backend.ShaderContext;
import com.jozufozu.flywheel.backend.ShaderSources;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.loading.InstancedArraysTemplate;
import com.jozufozu.flywheel.backend.loading.ProgramTemplate;
import com.jozufozu.flywheel.backend.loading.ShaderLoadingException;
import com.jozufozu.flywheel.backend.material.MaterialSpec;
import com.jozufozu.flywheel.backend.pipeline.IShaderPipeline;
import com.jozufozu.flywheel.backend.pipeline.LegacyPipeline;
import com.jozufozu.flywheel.core.shader.ExtensibleGlProgram;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;

import net.minecraft.util.ResourceLocation;

public class WorldContext<P extends WorldProgram> extends ShaderContext<P> {
	protected ResourceLocation name;
	protected Supplier<Stream<ResourceLocation>> specStream;
	protected TemplateFactory templateFactory;

	private final Map<ShaderType, ResourceLocation> builtins = new EnumMap<>(ShaderType.class);
	private final Map<ShaderType, String> builtinSources = new EnumMap<>(ShaderType.class);

	private final ExtensibleGlProgram.Factory<P> factory;

	public IShaderPipeline<P> pipeline;

	public WorldContext(Backend backend, ExtensibleGlProgram.Factory<P> factory) {
		super(backend);
		this.factory = factory;

		specStream = () -> backend.allMaterials()
				.stream()
				.map(MaterialSpec::getProgramName);

		templateFactory = InstancedArraysTemplate::new;
	}

	public WorldContext<P> withName(ResourceLocation name) {
		this.name = name;
		return this;
	}

	public WorldContext<P> withBuiltin(ShaderType shaderType, ResourceLocation folder, String file) {
		return withBuiltin(shaderType, ResourceUtil.subPath(folder, file));
	}

	public WorldContext<P> withBuiltin(ShaderType shaderType, ResourceLocation file) {
		builtins.put(shaderType, file);
		return this;
	}

	public WorldContext<P> withSpecStream(Supplier<Stream<ResourceLocation>> specStream) {
		this.specStream = specStream;
		return this;
	}

	public WorldContext<P> withTemplateFactory(TemplateFactory templateFactory) {
		this.templateFactory = templateFactory;
		return this;
	}

	@Override
	public void load() {

		Backend.log.info("Loading context '{}'", name);

		try {
			builtins.forEach((type, resourceLocation) -> builtinSources.put(type, backend.sources.getShaderSource(resourceLocation)));
		} catch (ShaderLoadingException e) {
			backend.sources.notifyError();

			Backend.log.error(String.format("Could not find builtin: %s", e.getMessage()));

			return;
		}

		pipeline = new LegacyPipeline<>(backend.sources, templateFactory.create(backend.sources), factory, builtinSources);

		specStream.get()
				.map(backend::getSpec)
				.forEach(this::loadSpec);
	}

	private void loadSpec(ProgramSpec spec) {

		try {
			programs.put(spec.name, pipeline.compile(spec));

			Backend.log.debug("Loaded program {}", spec.name);
		} catch (Exception e) {
			Backend.log.error("Program '{}': {}", spec.name, e);
			backend.sources.notifyError();
		}
	}

	public interface TemplateFactory {
		ProgramTemplate create(ShaderSources loader);
	}
}
