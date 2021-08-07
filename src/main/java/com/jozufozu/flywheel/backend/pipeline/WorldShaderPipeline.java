package com.jozufozu.flywheel.backend.pipeline;

import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;

import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.ShaderSources;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.shader.ExtensibleGlProgram;
import com.jozufozu.flywheel.core.shader.GameStateProgram;
import com.jozufozu.flywheel.core.shader.IMultiProgram;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;
import com.jozufozu.flywheel.core.shader.spec.ProgramState;

import net.minecraft.util.ResourceLocation;

public class WorldShaderPipeline<P extends WorldProgram> {

	private final ShaderSources sources;

	private final ExtensibleGlProgram.Factory<P> factory;

	public WorldShaderPipeline(ShaderSources sources, ExtensibleGlProgram.Factory<P> factory) {
		this.sources = sources;
		this.factory = factory;
	}

	public IMultiProgram<P> compile(ProgramSpec spec) {

		SourceFile file = sources.source(spec.vert);

		return compile(spec.name, file, spec.getStates());
	}

	public IMultiProgram<P> compile(ResourceLocation name, SourceFile file, List<ProgramState> variants) {
		ShaderBuilder shader = new ShaderBuilder(name, new Template())
				.setMainSource(file)
				.setVersion(GLSLVersion.V120);

		GameStateProgram.Builder<P> builder = GameStateProgram.builder(compile(shader, name, null));

		for (ProgramState variant : variants) {
			builder.withVariant(variant.getContext(), compile(shader, name, variant));
		}

		return builder.build();
	}

	private P compile(ShaderBuilder shader, ResourceLocation name, @Nullable ProgramState variant) {

		if (variant != null) {
			shader.setDefines(variant.getDefines());
		}

		GlShader vertex = shader.compile(name, ShaderType.VERTEX);
		GlShader fragment = shader.compile(name, ShaderType.FRAGMENT);

		int program = GL20.glCreateProgram();

		GL20.glAttachShader(program, vertex.handle());
		GL20.glAttachShader(program, fragment.handle());

		String log = glGetProgramInfoLog(program);

		if (!log.isEmpty()) {
			Backend.log.debug("Program link log for " + name + ": " + log);
		}

		int result = glGetProgrami(program, GL_LINK_STATUS);

		if (result != GL_TRUE) {
			throw new RuntimeException("Shader program linking failed, see log for details");
		}

		if (variant != null) {
			return factory.create(name, program, variant.getExtensions());
		} else {
			return factory.create(name, program, null);
		}
	}
}
