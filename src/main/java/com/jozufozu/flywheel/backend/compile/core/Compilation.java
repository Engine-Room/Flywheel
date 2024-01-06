package com.jozufozu.flywheel.backend.compile.core;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.gl.GlCompat;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.glsl.GlslVersion;
import com.jozufozu.flywheel.backend.glsl.SourceComponent;
import com.jozufozu.flywheel.backend.glsl.SourceFile;
import com.jozufozu.flywheel.lib.util.StringUtil;

import net.minecraft.client.Minecraft;

/**
 * Builder style class for compiling shaders.
 * <p>
 * Keeps track of the source files and components used to compile a shader,
 * and interprets/pretty prints any errors that occur.
 */
public class Compilation {
	public static final boolean DUMP_SHADER_SOURCE = System.getProperty("flw.dumpShaderSource") != null;

	private final List<SourceFile> files = new ArrayList<>();
	private final StringBuilder generatedSource;
	private final StringBuilder fullSource;
	private final GlslVersion glslVersion;
	private final ShaderType shaderType;
	private int generatedLines = 0;

	public Compilation(GlslVersion glslVersion, ShaderType shaderType) {
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

		var shaderName = shaderType.name + glslVersion + '_' + Integer.toUnsignedString(source.hashCode());
		dumpSource(source, shaderType.getFileName(shaderName));

		var infoLog = GL20.glGetShaderInfoLog(handle);

		if (compiledSuccessfully(handle)) {
			return ShaderResult.success(new GlShader(handle, shaderType, shaderName), infoLog);
		}

		GL20.glDeleteShader(handle);
		return ShaderResult.failure(new FailedCompilation(shaderName, files, generatedSource.toString(), source, infoLog));
	}

	public void enableExtension(String ext) {
		fullSource.append("#extension ")
				.append(ext)
				.append(" : enable\n");
	}

	public void define(String key, String value) {
		fullSource.append("#define ")
				.append(key)
				.append(' ')
				.append(value)
				.append('\n');
	}

	public void appendComponent(SourceComponent component) {
		var source = component.source();

		appendHeader(component, source);

		fullSource.append(source);
	}

	private void appendHeader(SourceComponent component, String source) {
		if (component instanceof SourceFile file) {
			int fileId = files.size() + 1;

			files.add(file);

			fullSource.append("\n#line 0 ")
					.append(fileId)
					.append(" // ")
					.append(file.name)
					.append('\n');
		} else {
			// Add extra newline to keep line numbers consistent
			generatedSource.append(source)
					.append('\n');

			fullSource.append("\n#line ")
					.append(generatedLines)
					.append(" 0 // (generated) ") // all generated code is put in file 0
					.append(component.name())
					.append('\n');

			generatedLines += StringUtil.countLines(source);
		}
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
