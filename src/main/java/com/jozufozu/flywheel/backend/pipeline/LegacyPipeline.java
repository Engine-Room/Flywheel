package com.jozufozu.flywheel.backend.pipeline;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.ShaderSources;
import com.jozufozu.flywheel.backend.gl.GlObject;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.loading.Program;
import com.jozufozu.flywheel.backend.loading.ProgramTemplate;
import com.jozufozu.flywheel.backend.loading.Shader;
import com.jozufozu.flywheel.backend.loading.ShaderLoadingException;
import com.jozufozu.flywheel.backend.loading.ShaderTransformer;
import com.jozufozu.flywheel.core.shader.ExtensibleGlProgram;
import com.jozufozu.flywheel.core.shader.GameStateProgram;
import com.jozufozu.flywheel.core.shader.IMultiProgram;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;
import com.jozufozu.flywheel.core.shader.spec.ProgramState;

public class LegacyPipeline<P extends WorldProgram> implements IShaderPipeline<P> {

	private static final String declaration = "#flwbuiltins";
	private static final Pattern builtinPattern = Pattern.compile(declaration);


	private final ShaderSources sources;
	protected ShaderTransformer transformer;
	private final ProgramTemplate template;
	private final ExtensibleGlProgram.Factory<P> factory;
	private final Map<ShaderType, String> builtinSources;


	public LegacyPipeline(ShaderSources sources, ProgramTemplate template, ExtensibleGlProgram.Factory<P> factory, Map<ShaderType, String> builtinSources) {
		this.sources = sources;
		this.template = template;
		this.factory = factory;

		transformer = new ShaderTransformer().pushStage(this::injectBuiltins)
				.pushStage(Shader::processIncludes)
				.pushStage(template)
				.pushStage(Shader::processIncludes);
		this.builtinSources = builtinSources;
	}

	@Override
	public IMultiProgram<P> compile(ProgramSpec spec) {
		GameStateProgram.Builder<P> builder = GameStateProgram.builder(compile(spec, null));

		for (ProgramState state : spec.states) {

			builder.withVariant(state.getContext(), compile(spec, state));
		}

		return builder.build();
	}

	/**
	 * Ingests the given shaders, compiling them and linking them together after applying the transformer to the source.
	 *
	 * @param shaders What are the different shader stages that should be linked together?
	 * @return A program with all provided shaders attached
	 */
	protected static Program buildProgram(Shader... shaders) {
		List<GlShader> compiled = new ArrayList<>(shaders.length);
		try {
			Program builder = new Program();

			for (Shader shader : shaders) {
				GlShader sh = new GlShader(shader.name, shader.type, shader.getSource());
				compiled.add(sh);

				builder.attachShader(shader, sh);
			}

			return builder;
		} finally {
			compiled.forEach(GlObject::delete);
		}
	}

	public Program loadAndLink(ProgramSpec spec, @Nullable ProgramState state) {
		Shader vertexFile = sources.source(spec.vert, ShaderType.VERTEX);
		Shader fragmentFile = sources.source(spec.frag, ShaderType.FRAGMENT);

		transformer.transformSource(vertexFile);
		transformer.transformSource(fragmentFile);

		if (state != null) {
			vertexFile.defineAll(state.getDefines());
			fragmentFile.defineAll(state.getDefines());
		}

		Program program = buildProgram(vertexFile, fragmentFile);
		template.attachAttributes(program);

		program.link(spec.name).deleteLinkedShaders();

		String descriptor = program.program + ": " + spec.name;

		if (state != null)
			descriptor += "#" + state;

		Backend.log.debug(descriptor);

		return program;
	}

	private P compile(ProgramSpec spec, @Nullable ProgramState state) {
		if (state != null) {
			Program program = loadAndLink(spec, state);
			return factory.create(program.name, program.program, state.getExtensions());
		} else {
			Program program = loadAndLink(spec, null);
			return factory.create(program.name, program.program, null);
		}
	}

	/**
	 * Replace #flwbuiltins with whatever expansion this context provides for the given shader.
	 */
	public void injectBuiltins(Shader shader) {
		Matcher matcher = builtinPattern.matcher(shader.getSource());

		if (matcher.find()) shader.setSource(matcher.replaceFirst(builtinSources.get(shader.type)));
		else
			throw new ShaderLoadingException(String.format("%s is missing %s, cannot use in World Context", shader.type.name, declaration));
	}
}
