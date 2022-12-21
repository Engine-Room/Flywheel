package com.jozufozu.flywheel.backend.gl.shader;

import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlObject;
import com.mojang.blaze3d.shaders.ProgramManager;

public class GlProgram extends GlObject {

	public GlProgram(int handle) {
		setHandle(handle);
	}

	// TODO: Programs bind the uniform buffers they need, no more GlProgram inheritance
	public void bind() {
		ProgramManager.glUseProgram(handle());
	}

	public static void unbind() {
		ProgramManager.glUseProgram(0);
	}

	/**
	 * Retrieves the index of the uniform with the given name.
	 *
	 * @param uniform The name of the uniform to find the index of
	 * @return The uniform's index
	 */
	public int getUniformLocation(String uniform) {
		int index = glGetUniformLocation(this.handle(), uniform);

		if (index < 0) {
			Backend.LOGGER.debug("No active uniform '{}' exists. Could be unused.", uniform);
		}

		return index;
	}

	/**
	 * Binds a sampler uniform to the given texture unit.
	 *
	 * @param name    The name of the sampler uniform.
	 * @param binding The index of the texture unit.
	 * @return The sampler uniform's index.
	 * @throws NullPointerException If no uniform exists with the given name.
	 */
	public int setSamplerBinding(String name, int binding) {
		int samplerUniform = getUniformLocation(name);

		if (samplerUniform >= 0) {
			glUniform1i(samplerUniform, binding);
		}

		return samplerUniform;
	}

	@Override
	protected void deleteInternal(int handle) {
		glDeleteProgram(handle);
	}

	/**
	 * A factory interface to create a {@link GlProgram}.
	 */
	public interface Factory {

		@NotNull GlProgram create(int handle);
	}
}
