package com.jozufozu.flywheel.backend.pipeline;

import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;

import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.FileResolution;
import com.jozufozu.flywheel.backend.ShaderSources;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.loading.ProtoProgram;
import com.jozufozu.flywheel.core.shader.ExtensibleGlProgram;
import com.jozufozu.flywheel.core.shader.GameStateProgram;
import com.jozufozu.flywheel.core.shader.IMultiProgram;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;
import com.jozufozu.flywheel.core.shader.spec.ProgramState;

import net.minecraft.util.ResourceLocation;

public class WorldShaderPipeline<P extends WorldProgram> implements IShaderPipeline<P> {

	private final ShaderSources sources;

	private final ExtensibleGlProgram.Factory<P> factory;

	private final ITemplate template;
	private final FileResolution header;

	public WorldShaderPipeline(ShaderSources sources, ExtensibleGlProgram.Factory<P> factory, ITemplate template, FileResolution header) {
		this.sources = sources;
		this.factory = factory;
		this.template = template;
		this.header = header;
	}

	public IMultiProgram<P> compile(ProgramSpec spec) {

		SourceFile file = sources.source(spec.vert);

		return compile(spec.name, file, spec.getStates());
	}

	public IMultiProgram<P> compile(ResourceLocation name, SourceFile file, List<ProgramState> variants) {
		ShaderBuilder shader = new ShaderBuilder(name, template, header)
				.setMainSource(file)
				.setVersion(GLSLVersion.V110);

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

		ProtoProgram program = new ProtoProgram();

		program.attachShader(vertex);
		program.attachShader(fragment);

		template.attachAttributes(program, shader.mainFile);

		program.link(name);
		program.deleteLinkedShaders();

		if (variant != null) {
			return factory.create(name, program.program, variant.getExtensions());
		} else {
			return factory.create(name, program.program, null);
		}
	}
}
