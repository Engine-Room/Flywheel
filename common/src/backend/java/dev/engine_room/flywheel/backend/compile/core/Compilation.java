package dev.engine_room.flywheel.backend.compile.core;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL20;

import dev.engine_room.flywheel.backend.compile.FlwPrograms;
import dev.engine_room.flywheel.backend.gl.GlCompat;
import dev.engine_room.flywheel.backend.gl.shader.GlShader;
import dev.engine_room.flywheel.backend.gl.shader.ShaderType;
import dev.engine_room.flywheel.backend.glsl.GlslVersion;
import dev.engine_room.flywheel.backend.glsl.SourceComponent;
import dev.engine_room.flywheel.backend.glsl.SourceFile;
import dev.engine_room.flywheel.lib.util.StringUtil;
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
	private final StringBuilder generatedSource = new StringBuilder();
	private final StringBuilder fullSource = new StringBuilder();
	private int generatedLines = 0;

	public ShaderResult compile(ShaderType shaderType, String name) {
		int handle = GL20.glCreateShader(shaderType.glEnum);
		var source = fullSource.toString();

		GlCompat.safeShaderSource(handle, source);
		GL20.glCompileShader(handle);

		var shaderName = name + "." + shaderType.extension;
		dumpSource(source, shaderName);

		var infoLog = GL20.glGetShaderInfoLog(handle);

		if (compiledSuccessfully(handle)) {
			return ShaderResult.success(new GlShader(handle, shaderType, shaderName), infoLog);
		}

		GL20.glDeleteShader(handle);
		return ShaderResult.failure(new FailedCompilation(shaderName, files, generatedSource.toString(), source, infoLog));
	}

	public void version(GlslVersion version) {
		fullSource.append("#version ")
				.append(version.version)
				.append('\n');
	}

	public void enableExtension(String ext) {
		fullSource.append("#extension ")
				.append(ext)
				.append(" : enable\n");
	}

	public void requireExtension(String ext) {
		fullSource.append("#extension ")
				.append(ext)
				.append(" : require\n");
	}

	public void define(String key, String value) {
		fullSource.append("#define ")
				.append(key)
				.append(' ')
				.append(value)
				.append('\n');
	}

	public void define(String key) {
		fullSource.append("#define ")
				.append(key)
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
					.append(file.name())
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

		File file = new File(new File(Minecraft.getInstance().gameDirectory, "flywheel_sources"), fileName);
		// mkdirs of the parent so we don't create a directory named by the leaf file we want to write
		file.getParentFile()
				.mkdirs();
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(source);
		} catch (Exception e) {
			FlwPrograms.LOGGER.error("Could not dump source.", e);
		}
	}

	public static boolean compiledSuccessfully(int handle) {
		return GL20.glGetShaderi(handle, GL20.GL_COMPILE_STATUS) == GL20.GL_TRUE;
	}
}
