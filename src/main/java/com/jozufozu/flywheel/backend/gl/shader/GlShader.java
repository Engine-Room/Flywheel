package com.jozufozu.flywheel.backend.gl.shader;

import java.util.List;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlObject;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;
import com.jozufozu.flywheel.backend.pipeline.ShaderCompiler;
import com.jozufozu.flywheel.backend.source.ShaderLoadingException;
import com.jozufozu.flywheel.backend.source.error.ErrorBuilder;
import com.jozufozu.flywheel.backend.source.error.ErrorReporter;

import net.minecraft.resources.ResourceLocation;

public class GlShader extends GlObject {

	public final ResourceLocation name;
	public final ShaderType type;

	public GlShader(ShaderCompiler env, ShaderType type, String source) {
		name = env.name;
		this.type = type;
		int handle = GL20.glCreateShader(type.glEnum);

		GlCompat.safeShaderSource(handle, source);
		GL20.glCompileShader(handle);

		String log = GL20.glGetShaderInfoLog(handle);

		if (!log.isEmpty()) {
			List<String> lines = log.lines()
					.toList();

			boolean needsSourceDump = false;

			StringBuilder errors = new StringBuilder();
			for (String line : lines) {
				ErrorBuilder builder = env.parseCompilerError(line);

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

		if (GL20.glGetShaderi(handle, GL20.GL_COMPILE_STATUS) != GL20.GL_TRUE) {
			throw new ShaderLoadingException("Could not compile " + name + ". See log for details.");
		}

		setHandle(handle);
	}

	@Override
	protected void deleteInternal(int handle) {
		GL20.glDeleteShader(handle);
	}
}
