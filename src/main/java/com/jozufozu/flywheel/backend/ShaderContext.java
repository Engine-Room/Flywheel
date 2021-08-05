package com.jozufozu.flywheel.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.backend.gl.GlObject;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.loading.Program;
import com.jozufozu.flywheel.backend.loading.Shader;
import com.jozufozu.flywheel.core.shader.IMultiProgram;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;
import com.jozufozu.flywheel.core.shader.spec.ProgramState;

public abstract class ShaderContext<P extends GlProgram> implements IShaderContext<P> {

	protected final Map<ResourceLocation, IMultiProgram<P>> programs = new HashMap<>();

	public final Backend backend;

	public ShaderContext(Backend backend) {
		this.backend = backend;
	}

	@Override
	public Supplier<P> getProgramSupplier(ResourceLocation spec) {
		return programs.get(spec);
	}

	public Program loadAndLink(ProgramSpec spec, @Nullable ProgramState state) {
		Shader vertexFile = getSource(ShaderType.VERTEX, spec.vert);
		Shader fragmentFile = getSource(ShaderType.FRAGMENT, spec.frag);

		if (state != null) {
			vertexFile.defineAll(state.getDefines());
			fragmentFile.defineAll(state.getDefines());
		}

		Program linked = link(buildProgram(spec.name, vertexFile, fragmentFile));

		String descriptor = linked.program + ": " + spec.name;

		if (state != null)
			descriptor += "#" + state;

		Backend.log.debug(descriptor);

		return linked;
	}

	protected Shader getSource(ShaderType type, ResourceLocation name) {
		return backend.sources.source(name, type);
	}

	protected Program link(Program program) {
		return program.link().deleteLinkedShaders();
	}

	@Override
	public void delete() {
		programs.values()
				.forEach(IMultiProgram::delete);
		programs.clear();
	}

	/**
	 * Ingests the given shaders, compiling them and linking them together after applying the transformer to the source.
	 *
	 * @param name    What should we call this program if something goes wrong?
	 * @param shaders What are the different shader stages that should be linked together?
	 * @return A program with all provided shaders attached
	 */
	protected static Program buildProgram(ResourceLocation name, Shader... shaders) {
		List<GlShader> compiled = new ArrayList<>(shaders.length);
		try {
			Program builder = new Program(name);

			for (Shader shader : shaders) {
				GlShader sh = new GlShader(shader);
				compiled.add(sh);

				builder.attachShader(shader, sh);
			}

			return builder;
		} finally {
			compiled.forEach(GlObject::delete);
		}
	}

}
