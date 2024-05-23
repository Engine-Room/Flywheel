package dev.engine_room.flywheel.backend.gl.shader;

import static org.lwjgl.opengl.GL20.glBindAttribLocation;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUniformMatrix3fv;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL30.glUniform1ui;
import static org.lwjgl.opengl.GL31.GL_INVALID_INDEX;
import static org.lwjgl.opengl.GL31.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL31.glUniformBlockBinding;

import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.slf4j.Logger;

import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.logging.LogUtils;

import dev.engine_room.flywheel.backend.gl.GlObject;
import dev.engine_room.flywheel.backend.gl.GlTextureUnit;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class GlProgram extends GlObject {
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

	public void setFloat(String glslName, float value) {
		int uniform = getUniformLocation(glslName);

		if (uniform < 0) {
			return;
		}

		glUniform1f(uniform, value);
	}

	public void setVec2(String glslName, float x, float y) {
		int uniform = getUniformLocation(glslName);

		if (uniform < 0) {
			return;
		}

		glUniform2f(uniform, x, y);
	}

	public void setVec3(String glslName, float x, float y, float z) {
		int uniform = getUniformLocation(glslName);

		if (uniform < 0) {
			return;
		}

		glUniform3f(uniform, x, y, z);
	}

	public void setVec4(String glslName, float x, float y, float z, float w) {
		int uniform = getUniformLocation(glslName);

		if (uniform < 0) {
			return;
		}

		glUniform4f(uniform, x, y, z, w);
	}

	public void setMat4(String glslName, Matrix4fc matrix) {
		int uniform = getUniformLocation(glslName);

		if (uniform < 0) {
			return;
		}

		glUniformMatrix4fv(uniform, false, matrix.get(new float[16]));
	}

	public void setMat3(String glslName, Matrix3fc matrix) {
		int uniform = getUniformLocation(glslName);

		if (uniform < 0) {
			return;
		}

		glUniformMatrix3fv(uniform, false, matrix.get(new float[9]));
	}

	public void setBool(String glslName, boolean bool) {
		setInt(glslName, bool ? 1 : 0);
	}

	public void setUInt(String glslName, int value) {
		int uniform = getUniformLocation(glslName);

		if (uniform < 0) {
			return;
		}

		glUniform1ui(uniform, value);
	}

	public void setInt(String glslName, int value) {
		int uniform = getUniformLocation(glslName);

		if (uniform < 0) {
			return;
		}

		glUniform1i(uniform, value);
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
	public void setSamplerBinding(String name, GlTextureUnit binding) {
		setSamplerBinding(name, binding.number);
	}

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

	public void bindAttribLocation(String attribute, int binding) {
		glBindAttribLocation(handle(), binding, attribute);
	}

	@Override
	protected void deleteInternal(int handle) {
		glDeleteProgram(handle);
	}
}
