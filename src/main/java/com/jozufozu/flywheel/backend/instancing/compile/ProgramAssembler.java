package com.jozufozu.flywheel.backend.instancing.compile;

import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glLinkProgram;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;

@Deprecated
public class ProgramAssembler {
	private final int program;

	public ProgramAssembler() {
		this.program = glCreateProgram();
	}

	/**
	 * Links the attached shaders to this program.
	 */
	public int link() {
		glLinkProgram(this.program);

		String log = glGetProgramInfoLog(this.program);

		if (!log.isEmpty()) {
			Backend.LOGGER.debug("Program link log: " + log);
		}

		int result = glGetProgrami(this.program, GL_LINK_STATUS);

		if (result != GL_TRUE) {
			throw new RuntimeException("Shader program linking failed, see log for details");
		}

		return program;
	}

	public ProgramAssembler attachShader(GlShader glShader) {
		glAttachShader(this.program, glShader.handle());
		return this;
	}
}
