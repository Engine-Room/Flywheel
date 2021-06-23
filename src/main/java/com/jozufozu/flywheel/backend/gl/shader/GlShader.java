package com.jozufozu.flywheel.backend.gl.shader;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlObject;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;
import com.jozufozu.flywheel.backend.loading.Shader;

import net.minecraft.util.ResourceLocation;

public class GlShader extends GlObject {

	public final ResourceLocation name;
	public final ShaderType type;

	public GlShader(Shader shader) {
		this.type = shader.type;
		this.name = shader.name;
		int handle = GL20.glCreateShader(shader.type.glEnum);

		GlCompat.safeShaderSource(handle, shader.getSource());
		GL20.glCompileShader(handle);

		String log = GL20.glGetShaderInfoLog(handle);

		if (!log.isEmpty()) {
			Backend.log.error("Shader compilation log for " + shader.name + ": " + log);
			Backend.log.error(shader.printSource());
		}

		if (GL20.glGetShaderi(handle, GL20.GL_COMPILE_STATUS) != GL20.GL_TRUE) {
			throw new RuntimeException("Could not compile " + shader.name + ". See log for details.");
		}

		setHandle(handle);
	}

	@Override
	protected void deleteInternal(int handle) {
		GL20.glDeleteShader(handle);
	}
}
