package com.jozufozu.flywheel.backend.loading;

import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_TRUE;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glBindAttribLocation;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glLinkProgram;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.IntConsumer;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.ResourceLocation;

public class ProtoProgram {
	public final int program;

	public ResourceLocation name;
	private int attributeIndex;

	private final IntList shaders;

	public ProtoProgram() {
		this.program = glCreateProgram();
		shaders = new IntArrayList(2);
	}

	public void attachShader(GlShader glShader) {
		glAttachShader(this.program, glShader.handle());
	}

	public void addAttribute(String name, int attributeCount) {
		glBindAttribLocation(this.program, attributeIndex, name);
		attributeIndex += attributeCount;
	}

	/**
	 * Links the attached shaders to this program.
	 */
	public ProtoProgram link(ResourceLocation name) {
		this.name = name;
		glLinkProgram(this.program);

		String log = glGetProgramInfoLog(this.program);

		if (!log.isEmpty()) {
			Backend.log.debug("Program link log for " + name + ": " + log);
		}

		int result = glGetProgrami(this.program, GL_LINK_STATUS);

		if (result != GL_TRUE) {
			throw new RuntimeException("Shader program linking failed, see log for details");
		}

		return this;
	}

	public ProtoProgram deleteLinkedShaders() {
		shaders.forEach((IntConsumer) GL20::glDeleteShader);
		return this;
	}
}
