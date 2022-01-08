package com.jozufozu.flywheel.core.compile;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.source.FileResolution;
import com.jozufozu.flywheel.backend.source.SourceFile;
import com.jozufozu.flywheel.backend.source.error.ErrorBuilder;
import com.jozufozu.flywheel.backend.source.error.ErrorReporter;
import com.jozufozu.flywheel.backend.source.span.Span;
import com.jozufozu.flywheel.core.shader.WorldProgram;

import net.minecraft.resources.ResourceLocation;

/**
 * Compiles a shader program.
 */
public class ShaderCompiler implements FileIndex {

	/**
	 * The name of the file responsible for this compilation.
	 */
	public final ResourceLocation name;

	/**
	 * The template we'll be using to generate the final source.
	 */
	public final Template template;

	private final FileResolution header;

	/**
	 * Extra {@code #define}s to be added to the shader.
	 */
	private final List<String> defines;

	/**
	 * Alpha threshold below which pixels are discarded.
	 */
	private final float alphaDiscard;

	/**
	 * The vertex type to use.
	 */
	public final VertexType vertexType;

	/**
	 * The main file to compile.
	 */
	public final SourceFile mainFile;

	private final List<SourceFile> files = new ArrayList<>();

	public ShaderCompiler(ProgramContext context, Template template, FileResolution header) {
		this.name = context.spec().name;
		this.template = template;
		this.header = header;
		this.mainFile = context.getFile();
		this.defines = context.spec()
				.getDefines(context.ctx());
		this.vertexType = context.vertexType();
		this.alphaDiscard = context.alphaDiscard();
	}

	public GlShader compile(ShaderType type) {

		StringBuilder finalSource = new StringBuilder();

		finalSource.append("#version ")
				.append(template.getVersion())
				.append('\n')
				.append("#extension GL_ARB_explicit_attrib_location : enable\n")
				.append("#extension GL_ARB_conservative_depth : enable\n")
				.append(type.getDefineStatement()); // special case shader type declaration

		if (alphaDiscard > 0) {
			finalSource.append("#define ALPHA_DISCARD 0.1\n");
		}

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

	public <P extends WorldProgram> P compile(GlProgram.Factory<P> worldShaderPipeline) {
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
	@Override
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
