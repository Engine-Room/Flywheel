package com.jozufozu.flywheel.core.pipeline;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.source.FileResolution;
import com.jozufozu.flywheel.backend.source.SourceFile;
import com.jozufozu.flywheel.backend.source.error.ErrorBuilder;
import com.jozufozu.flywheel.backend.source.error.ErrorReporter;
import com.jozufozu.flywheel.backend.source.span.Span;
import com.jozufozu.flywheel.core.shader.ExtensibleGlProgram;
import com.jozufozu.flywheel.core.shader.WorldProgram;

import net.minecraft.resources.ResourceLocation;

public class ShaderCompiler {

	public final ResourceLocation name;
	public final Template<?> template;
	private final FileResolution header;

	private final List<String> defines;

	public final VertexType vertexType;

	public final SourceFile mainFile;

	private final List<SourceFile> files = new ArrayList<>();

	public ShaderCompiler(CompilationContext usage, Template<?> template, FileResolution header) {
		this.name = usage.spec().name;
		this.template = template;
		this.header = header;
		this.mainFile = usage.getFile();
		this.defines = usage.spec()
				.getDefines(usage.ctx());
		this.vertexType = usage.vertexType();
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

		for (String def : defines) {
			finalSource.append("#define ")
					.append(def)
					.append('\n');
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
		header.getFile().generateFinalSource(this, finalSource);
		mainFile.generateFinalSource(this, finalSource);

		template.getMetadata(mainFile).generateFooter(finalSource, type, this);

		return new GlShader(this, type, finalSource.toString());
	}

	public <P extends WorldProgram> P compile(ExtensibleGlProgram.Factory<P> worldShaderPipeline) {
		return new ProgramAssembler(this.name)
				.attachShader(compile(ShaderType.VERTEX))
				.attachShader(compile(ShaderType.FRAGMENT))
				.link()
				.deleteLinkedShaders()
				.build(worldShaderPipeline);
	}

	/**
	 * Returns an arbitrary file ID for use this compilation context, or generates one if missing.
	 * @param sourceFile The file to retrieve the ID for.
	 * @return A file ID unique to the given sourceFile.
	 */
	public int getFileID(SourceFile sourceFile) {
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

	public void printShaderInfoLog(String source, String log, ResourceLocation name) {
		List<String> lines = log.lines()
				.toList();

		boolean needsSourceDump = false;

		StringBuilder errors = new StringBuilder();
		for (String line : lines) {
			ErrorBuilder builder = parseCompilerError(line);

			if (builder != null) {
				errors.append(builder.build());
			} else {
				errors.append(line).append('\n');
				needsSourceDump = true;
			}
		}
		Backend.LOGGER.error("Errors compiling '" + name + "': \n" + errors);
		if (needsSourceDump) {
			// TODO: generated code gets its own "file"
			ErrorReporter.printLines(source);
		}
	}

	@Nullable
	private ErrorBuilder parseCompilerError(String line) {
		try {
			ErrorBuilder error = ErrorBuilder.fromLogLine(this, line);
			if (error != null) {
				return error;
			}
		} catch (Exception ignored) {
		}

		return null;
	}
}
