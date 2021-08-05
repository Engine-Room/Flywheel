package com.jozufozu.flywheel.backend.pipeline;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlObject;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;

public class GlShader extends GlObject {

	public GlShader(ShaderType type, CharSequence source) {
		int handle = GL20.glCreateShader(type.glEnum);

		GlCompat.safeShaderSource(handle, source);

		GL20.glCompileShader(handle);

		String log = GL20.glGetShaderInfoLog(handle);

		if (!log.isEmpty()) {
			Backend.log.error("Shader compilation log for " + "DUBUG"+ ": " + log);
			Backend.log.error(source);
		}
		//Backend.log.debug(shader.printSource());

		if (GL20.glGetShaderi(handle, GL20.GL_COMPILE_STATUS) != GL20.GL_TRUE) {
			throw new RuntimeException("Could not compile " + "DEBUG" + ". See log for details.");
		}


		setHandle(handle);
	}

	@Override
	protected void deleteInternal(int handle) {

	}
}
