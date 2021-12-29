package com.jozufozu.flywheel.backend.pipeline;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.source.FileResolution;
import com.jozufozu.flywheel.backend.source.SourceFile;
import com.jozufozu.flywheel.backend.source.error.ErrorBuilder;
import com.jozufozu.flywheel.backend.source.span.Span;
import com.jozufozu.flywheel.core.shader.ExtensibleGlProgram;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.shader.spec.ProgramState;

import net.minecraft.resources.ResourceLocation;

public class ShaderCompiler {

	public final ResourceLocation name;
	public final Template<?> template;
	private final FileResolution header;

	@Nullable
	private ProgramState variant;

	public final VertexType vertexType;

	public SourceFile mainFile;

	public ShaderCompiler(ResourceLocation name, SourceFile mainSource, Template<?> template, FileResolution header, VertexType vertexType) {
		this.name = name;
		this.template = template;
		this.header = header;
		this.mainFile = mainSource;
		this.vertexType = vertexType;
	}

	public ShaderCompiler setMainSource(SourceFile file) {
		if (mainFile == file) return this;

		mainFile = file;

		return this;
	}

	public GlShader compile(ShaderType type) {

		StringBuilder finalSource = new StringBuilder();

		finalSource.append("#version ")
				.append(template.getVersion())
				.append('\n')
				.append("#extension GL_ARB_explicit_attrib_location : enable\n")
				.append("#extension GL_ARB_conservative_depth : enable\n")
				.append("#define ")
				.append(type.define) // special case shader type declaration
				.append('\n');

		ProgramState variant = getVariant();
		if (variant != null) {
			for (String def : variant.defines()) {
				finalSource.append("#define ")
						.append(def)
						.append('\n');
			}
		}

		if (type == ShaderType.VERTEX) {
			finalSource.append("""
					struct Vertex {
					    vec3 pos;
					    vec4 color;
					    vec2 texCoords;
					    vec2 light;
					    vec3 normal;
					};
					""");
			finalSource.append(vertexType.writeShaderHeader());
		}

		files.clear();
		if (header.getFile() != null) {
			header.getFile().generateFinalSource(this, finalSource);
		}
		mainFile.generateFinalSource(this, finalSource);

		template.getMetadata(mainFile).generateFooter(finalSource, type, this);

		return new GlShader(this, type, finalSource.toString());
	}

	@Nullable
	public ProgramState getVariant() {
		return variant;
	}

	public void setVariant(@Nullable ProgramState variant) {
		this.variant = variant;
	}

	private final List<SourceFile> files = new ArrayList<>();

	public int allocateFile(SourceFile sourceFile) {
		int i = files.indexOf(sourceFile);
		if (i != -1) {
			return i;
		}

		int size = files.size();
		files.add(sourceFile);
		return size;
	}

	public Span getLineSpan(int fileId, int lineNo) {
		SourceFile file = files.get(fileId);

		return file.getLineSpanNoWhitespace(lineNo);
	}

	@Nullable
	public ErrorBuilder parseCompilerError(String line) {
		try {
			ErrorBuilder error = ErrorBuilder.fromLogLine(this, line);
			if (error != null) {
				return error;
			}
		} catch (Exception ignored) {
		}

		return null;
	}

	public <P extends WorldProgram> P compile(ExtensibleGlProgram.Factory<P> worldShaderPipeline) {
		return new ProgramAssembler(this.name)
				.attachShader(compile(ShaderType.VERTEX))
				.attachShader(compile(ShaderType.FRAGMENT))
				.link()
				.deleteLinkedShaders()
				.build(worldShaderPipeline);
	}
}
