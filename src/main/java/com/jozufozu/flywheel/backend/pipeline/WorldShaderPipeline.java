package com.jozufozu.flywheel.backend.pipeline;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.source.FileResolution;
import com.jozufozu.flywheel.backend.source.SourceFile;
import com.jozufozu.flywheel.core.shader.ContextAwareProgram;
import com.jozufozu.flywheel.core.shader.ExtensibleGlProgram;
import com.jozufozu.flywheel.core.shader.GameStateProgram;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;
import com.jozufozu.flywheel.core.shader.spec.ProgramState;

public class WorldShaderPipeline<P extends WorldProgram> implements ShaderPipeline<P> {

	private final ExtensibleGlProgram.Factory<P> factory;

	private final Template<?> template;
	private final FileResolution header;

	public WorldShaderPipeline(ExtensibleGlProgram.Factory<P> factory, Template<?> template, FileResolution header) {
		this.factory = factory;
		this.template = template;
		this.header = header;
	}

	public ContextAwareProgram<P> compile(ProgramSpec spec, VertexType vertexType) {

		SourceFile file = spec.getSource().getFile();

		ShaderCompiler shader = new ShaderCompiler(spec.name, file, template, header, vertexType);

		GameStateProgram.Builder<P> builder = GameStateProgram.builder(shader.compile(this.factory));
		for (ProgramState variant : spec.getStates()) {
			shader.setVariant(variant);
			builder.withVariant(variant.context(), shader.compile(this.factory));
		}

		return builder.build();
	}
}
