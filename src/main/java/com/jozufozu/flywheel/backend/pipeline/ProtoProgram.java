package com.jozufozu.flywheel.backend.pipeline;

import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glBindAttribLocation;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glLinkProgram;

import java.util.List;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ProtoProgram {
	public final int program;
	public final WorldShader parent;

	private int attributeIndex;

	private final List<GlShader> shaders;

	public ProtoProgram(WorldShader parent) {
		this.parent = parent;
		this.program = glCreateProgram();
		shaders = new ObjectArrayList<>();
	}

	public ProtoProgram compilePart(ShaderType type) {
		GlShader shader = parent.compile(type);
		attachShader(shader);
		return this;
	}

	/**
	 * Links the attached shaders to this program.
	 */
	public ProtoProgram link() {

		parent.template.getShaderInputs(parent.mainFile)
				.forEach(this::addAttribute);

		glLinkProgram(this.program);

		String log = glGetProgramInfoLog(this.program);

		if (!log.isEmpty()) {
			Backend.log.debug("Program link log for " + parent.name + ": " + log);
		}

		int result = glGetProgrami(this.program, GL_LINK_STATUS);

		if (result != GL_TRUE) {
			throw new RuntimeException("Shader program linking failed, see log for details");
		}

		return this;
	}

	public ProtoProgram deleteLinkedShaders() {
		shaders.forEach(GlShader::delete);
		return this;
	}

	private void attachShader(GlShader glShader) {
		shaders.add(glShader);
		glAttachShader(this.program, glShader.handle());
	}

	private void addAttribute(ShaderInput shaderInput) {
		glBindAttribLocation(this.program, attributeIndex, shaderInput.name);
		attributeIndex += shaderInput.attribCount;
	}

}
