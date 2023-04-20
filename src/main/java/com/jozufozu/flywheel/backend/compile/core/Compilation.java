package com.jozufozu.flywheel.backend.compile.core;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.gl.shader.GlShader;
import com.jozufozu.flywheel.gl.shader.ShaderType;
import com.jozufozu.flywheel.gl.versioned.GlCompat;
import com.jozufozu.flywheel.glsl.GLSLVersion;
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
	public static final boolean DUMP_SHADER_SOURCE = System.getProperty("flw.dumpShaderSource") != null;

	private final List<SourceFile> files = new ArrayList<>();
	private final List<ResourceLocation> componentNames = new ArrayList<>();
	private final StringBuilder generatedSource;
	private final StringBuilder fullSource;
	private final GLSLVersion glslVersion;
	private final ShaderType shaderType;
	private int generatedLines = 0;

	public Compilation(GLSLVersion glslVersion, ShaderType shaderType) {
		this.glslVersion = glslVersion;
		this.shaderType = shaderType;

		generatedSource = new StringBuilder();
		fullSource = new StringBuilder(glslVersion.getVersionLine()).append(shaderType.getDefineStatement()).append('\n');
	}

	@NotNull
	public ShaderResult compile() {
		int handle = GL20.glCreateShader(shaderType.glEnum);
		var source = fullSource.toString();

		GlCompat.safeShaderSource(handle, source);
		GL20.glCompileShader(handle);

		var shaderName = buildShaderName();
		dumpSource(source, shaderType.getFileName(shaderName));

		var infoLog = GL20.glGetShaderInfoLog(handle);

		if (compiledSuccessfully(handle)) {
			return ShaderResult.success(new GlShader(handle, shaderType, shaderName), infoLog);
		}

		GL20.glDeleteShader(handle);
		return ShaderResult.failure(new FailedCompilation(shaderName, files, generatedSource.toString(), infoLog));
	}

	public void enableExtension(String ext) {
		fullSource.append("#extension ")
				.append(ext)
				.append(" : enable\n");
	}

	public void appendComponent(SourceComponent component) {
		var source = component.source();

		if (component instanceof SourceFile file) {
			fullSource.append(sourceHeader(file));
		} else {
			fullSource.append(generatedHeader(source, component.name()
					.toString()));
		}

		fullSource.append(source);
		componentNames.add(component.name());
	}

	private String sourceHeader(SourceFile sourceFile) {
		return '\n' + "#line " + 0 + ' ' + getOrCreateFileID(sourceFile) + " // " + sourceFile.name + '\n';
	}

	private String generatedHeader(String generatedCode, String comment) {
		generatedSource.append(generatedCode);
		int lines = StringUtil.countLines(generatedCode);

		// all generated code is put in file 0,
		var out = '\n' + "#line " + generatedLines + ' ' + 0;

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
		// TODO: This name is so long it fails to create the file. Use index and map indices to component sources in separate file?
		var components = componentNames.stream()
				.map(ResourceLocation::toString)
				.map(s -> s.replaceAll("/", "_")
						.replaceAll(":", "\\$"))
				.collect(Collectors.joining(";"));
		return shaderType.name + glslVersion + ';' /*+ components*/;
	}

	private static void dumpSource(String source, String fileName) {
		if (!DUMP_SHADER_SOURCE) {
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
