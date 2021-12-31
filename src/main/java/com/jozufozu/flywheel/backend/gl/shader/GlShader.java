package com.jozufozu.flywheel.backend.gl.shader;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.gl.GlObject;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;
import com.jozufozu.flywheel.backend.source.ShaderLoadingException;
import com.jozufozu.flywheel.core.pipeline.ShaderCompiler;

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
			env.printShaderInfoLog(source, log, this.name);
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
