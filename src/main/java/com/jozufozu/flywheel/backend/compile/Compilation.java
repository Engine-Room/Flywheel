package com.jozufozu.flywheel.backend.compile;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.BackendUtil;
import com.jozufozu.flywheel.gl.GLSLVersion;
import com.jozufozu.flywheel.gl.shader.GlShader;
import com.jozufozu.flywheel.gl.shader.ShaderType;
import com.jozufozu.flywheel.gl.versioned.GlCompat;
import com.jozufozu.flywheel.glsl.SourceComponent;
import com.jozufozu.flywheel.glsl.SourceFile;
import com.jozufozu.flywheel.util.StringUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

/**
 * Builder style class for compiling shaders.
 * <p>
 * Keeps track of the source files and components used to compile a shader,
 * and interprets/pretty prints any errors that occur.
 */
public class Compilation {
	private final List<SourceFile> files = new ArrayList<>();
	private final List<ResourceLocation> componentNames = new ArrayList<>();
	private final StringBuilder generatedSource;
	private final StringBuilder fullSource;
	private final GLSLVersion glslVersion;
	private final ShaderType shaderType;
	private int generatedLines = 0;

	public Compilation(GLSLVersion glslVersion, ShaderType shaderType) {
		this.generatedSource = new StringBuilder();
		this.fullSource = new StringBuilder(CompileUtil.generateHeader(glslVersion, shaderType));
		this.glslVersion = glslVersion;
		this.shaderType = shaderType;
	}

	@NotNull
	public CompilationResult compile() {
		int handle = GL20.glCreateShader(shaderType.glEnum);
		var source = fullSource.toString();

		GlCompat.safeShaderSource(handle, source);
		GL20.glCompileShader(handle);

		var shaderName = buildShaderName();
		dumpSource(source, shaderType.getFileName(shaderName));

		if (compiledSuccessfully(handle)) {
			return CompilationResult.success(new GlShader(handle, shaderType, shaderName));
		}

		var errorLog = GL20.glGetShaderInfoLog(handle);
		GL20.glDeleteShader(handle);
		return CompilationResult.failure(new FailedCompilation(shaderName, files, generatedSource.toString(), errorLog));
	}

	public void enableExtension(String ext) {
		fullSource.append("#extension ")
				.append(ext)
				.append(" : enable\n");
	}

	public void addComponentName(ResourceLocation name) {
		componentNames.add(name);
	}

	public void appendComponent(SourceComponent component) {
		var source = component.source();

		if (component instanceof SourceFile file) {
			fullSource.append(sourceHeader(file));
		} else {
			fullSource.append(generatedHeader(source, component.name()
					.toString()));
		}

		fullSource.append(source)
				.append('\n');
	}

	private String sourceHeader(SourceFile sourceFile) {
		return "#line " + 0 + ' ' + getOrCreateFileID(sourceFile) + " // " + sourceFile.name + '\n';
	}

	private String generatedHeader(String generatedCode, String comment) {
		generatedSource.append(generatedCode);
		int lines = StringUtil.countLines(generatedCode);

		// all generated code is put in file 0,
		var out = "#line " + generatedLines + ' ' + 0;

		generatedLines += lines;

		return out + " // (generated) " + comment + '\n';
	}

	/**
	 * Returns an arbitrary file ID for use this compilation context, or generates one if missing.
	 *
	 * @param sourceFile The file to retrieve the ID for.
	 * @return A file ID unique to the given sourceFile.
	 */
	private int getOrCreateFileID(SourceFile sourceFile) {
		int i = files.indexOf(sourceFile);
		if (i != -1) {
			return i + 1;
		}

		files.add(sourceFile);
		return files.size();
	}

	@NotNull
	private String buildShaderName() {
		var components = componentNames.stream()
				.map(ResourceLocation::toString)
				.map(s -> s.replaceAll("/", "_")
						.replaceAll(":", "\\$"))
				.collect(Collectors.joining(";"));
		return shaderType.name + glslVersion + ';' + components;
	}

	private static void dumpSource(String source, String fileName) {
		if (!BackendUtil.DUMP_SHADER_SOURCE) {
			return;
		}

		File dir = new File(Minecraft.getInstance().gameDirectory, "flywheel_sources");
		dir.mkdirs();
		File file = new File(dir, fileName);
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(source);
		} catch (Exception e) {
			Flywheel.LOGGER.error("Could not dump source.", e);
		}
	}

	private static boolean compiledSuccessfully(int handle) {
		return GL20.glGetShaderi(handle, GL20.GL_COMPILE_STATUS) == GL20.GL_TRUE;
	}
}
