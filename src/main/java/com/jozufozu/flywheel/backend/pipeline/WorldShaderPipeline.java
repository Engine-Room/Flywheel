package com.jozufozu.flywheel.backend.pipeline;

import java.util.List;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.source.FileResolution;
import com.jozufozu.flywheel.backend.source.SourceFile;
import com.jozufozu.flywheel.core.shader.ExtensibleGlProgram;
import com.jozufozu.flywheel.core.shader.GameStateProgram;
import com.jozufozu.flywheel.core.shader.IMultiProgram;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;
import com.jozufozu.flywheel.core.shader.spec.ProgramState;

import net.minecraft.resources.ResourceLocation;

public class WorldShaderPipeline<P extends WorldProgram> implements IShaderPipeline<P> {

	private final ExtensibleGlProgram.Factory<P> factory;

	private final Template<?> template;
	private final FileResolution header;

	public WorldShaderPipeline(ExtensibleGlProgram.Factory<P> factory, Template<?> template, FileResolution header) {
		this.factory = factory;
		this.template = template;
		this.header = header;
	}

	public IMultiProgram<P> compile(ProgramSpec spec) {

		SourceFile file = spec.getSource().getFile();

		return compile(spec.name, file, spec.getStates());
	}

	public IMultiProgram<P> compile(ResourceLocation name, SourceFile file, List<ProgramState> variants) {
		WorldShader shader = new WorldShader(name, template, header)
				.setMainSource(file);

		GameStateProgram.Builder<P> builder = GameStateProgram.builder(compile(shader, null));

		for (ProgramState variant : variants) {
			builder.withVariant(variant.getContext(), compile(shader, variant));
		}

		return builder.build();
	}

	private P compile(WorldShader shader, @Nullable ProgramState variant) {

		if (variant != null) {
			shader.setDefines(variant.getDefines());
		}

		ProtoProgram program = shader.createProgram()
				.compilePart(ShaderType.VERTEX)
				.compilePart(ShaderType.FRAGMENT)
				.link()
				.deleteLinkedShaders();

		if (variant != null) {
			return factory.create(shader.name, program.program, variant.getExtensions());
		} else {
			return factory.create(shader.name, program.program, null);
		}
	}
}
