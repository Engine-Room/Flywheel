package com.jozufozu.flywheel.backend.gl.shader;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlObject;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;

import net.minecraft.resources.ResourceLocation;

public class GlShader extends GlObject {

	public final ResourceLocation name;
	public final ShaderType type;

	public GlShader(ResourceLocation name, ShaderType type, CharSequence source) {
		this.name = name;
		this.type = type;
		int handle = GL20.glCreateShader(type.glEnum);

		GlCompat.safeShaderSource(handle, source);
		GL20.glCompileShader(handle);

		String log = GL20.glGetShaderInfoLog(handle);

		if (!log.isEmpty()) {
			Backend.LOGGER.error("Shader compilation log for " + name + ": " + log);
			Backend.LOGGER.error(source);
		}
		//Backend.log.debug(shader.printSource());

		if (GL20.glGetShaderi(handle, GL20.GL_COMPILE_STATUS) != GL20.GL_TRUE) {
			throw new RuntimeException("Could not compile " + name + ". See log for details.");
		}

		setHandle(handle);
	}

	@Override
	protected void deleteInternal(int handle) {
		GL20.glDeleteShader(handle);
	}
}
