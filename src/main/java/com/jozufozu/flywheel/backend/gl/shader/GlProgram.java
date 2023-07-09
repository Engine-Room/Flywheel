package com.jozufozu.flywheel.backend.gl.shader;

import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

import java.nio.FloatBuffer;

import javax.annotation.Nonnull;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlObject;
import com.mojang.blaze3d.shaders.ProgramManager;

import net.minecraft.resources.ResourceLocation;

public abstract class GlProgram extends GlObject {
	private static final FloatBuffer floatBuffer = MemoryStack.stackGet()
			.mallocFloat(16);

	public final ResourceLocation name;

	protected GlProgram(ResourceLocation name, int handle) {
		this.name = name;
		setHandle(handle);
	}

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
			Backend.LOGGER.debug("No active uniform '{}' exists in program '{}'. Could be unused.", uniform, this.name);
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

	protected static void uploadMatrixUniform(int uniform, Matrix4f mat) {
		mat.get(floatBuffer);
		glUniformMatrix4fv(uniform, false, floatBuffer);
	}

	@Override
	protected void deleteInternal(int handle) {
		glDeleteProgram(handle);
	}

	@Override
	public String toString() {
		return "program " + name;
	}

	/**
	 * A factory interface to create a {@link GlProgram}.
	 */
	public interface Factory<P extends GlProgram> {

		@Nonnull
		P create(ResourceLocation name, int handle);
	}
}
