package com.jozufozu.flywheel.backend.gl.shader;

import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL31.GL_INVALID_INDEX;
import static org.lwjgl.opengl.GL31.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL31.glUniformBlockBinding;

import org.slf4j.Logger;

import com.jozufozu.flywheel.api.context.Shader;
import com.jozufozu.flywheel.api.context.Texture;
import com.jozufozu.flywheel.backend.engine.textures.IdentifiedTexture;
import com.jozufozu.flywheel.backend.engine.textures.TextureBinder;
import com.jozufozu.flywheel.backend.gl.GlObject;
import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.logging.LogUtils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class GlProgram extends GlObject implements Shader {
	private static final Logger LOGGER = LogUtils.getLogger();

	private final Object2IntMap<String> uniformLocationCache = new Object2IntOpenHashMap<>();

	public GlProgram(int handle) {
		handle(handle);
	}

	public void bind() {
		ProgramManager.glUseProgram(handle());
	}

	public static void unbind() {
		ProgramManager.glUseProgram(0);
	}

	@Override
	public void setTexture(String glslName, Texture texture) {
		if (!(texture instanceof IdentifiedTexture identified)) {
			return;
		}
		int uniform = getUniformLocation(glslName);

		if (uniform < 0) {
			return;
		}

		int id = identified.id();

		int binding = TextureBinder.bindTexture(id);

		glUniform1i(uniform, binding);
	}

	/**
	 * Retrieves the index of the uniform with the given name.
	 *
	 * @param uniform The name of the uniform to find the index of
	 * @return The uniform's index
	 */
	public int getUniformLocation(String uniform) {
		return uniformLocationCache.computeIfAbsent(uniform, s -> {
			int index = glGetUniformLocation(this.handle(), uniform);

			if (index < 0) {
				LOGGER.debug("No active uniform '{}' exists. Could be unused.", uniform);
			}
			return index;
		});
	}

	/**
	 * Binds a sampler uniform to the given texture unit.
	 *
	 * @param name    The name of the sampler uniform.
	 * @param binding The index of the texture unit.
	 */
	public void setSamplerBinding(String name, int binding) {
		int samplerUniform = getUniformLocation(name);

		if (samplerUniform >= 0) {
			glUniform1i(samplerUniform, binding);
		}
	}

	public void setUniformBlockBinding(String name, int binding) {
		int index = glGetUniformBlockIndex(handle(), name);

		if (index == GL_INVALID_INDEX) {
			LOGGER.debug("No active uniform block '{}' exists. Could be unused.", name);
			return;
		}

		glUniformBlockBinding(handle(), index, binding);
	}

	@Override
	protected void deleteInternal(int handle) {
		glDeleteProgram(handle);
	}
}
