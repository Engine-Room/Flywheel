package com.jozufozu.flywheel.core.pipeline;

import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.source.FileResolution;
import com.jozufozu.flywheel.core.shader.ExtensibleGlProgram;
import com.jozufozu.flywheel.core.shader.WorldProgram;

public class WorldCompiler<P extends WorldProgram> implements PipelineCompiler<P> {

	private final ExtensibleGlProgram.Factory<P> factory;

	private final Template<?> template;
	private final FileResolution header;

	public WorldCompiler(ExtensibleGlProgram.Factory<P> factory, Template<?> template, FileResolution header) {
		this.factory = factory;
		this.template = template;
		this.header = header;
	}

	@Override
	public P compile(CompilationContext usage) {
		ShaderCompiler compiler = new ShaderCompiler(usage, template, header);

		return new ProgramAssembler(compiler.name)
				.attachShader(compiler.compile(ShaderType.VERTEX))
				.attachShader(compiler.compile(ShaderType.FRAGMENT))
				.link()
				.deleteLinkedShaders()
				.build(this.factory);
	}

}
