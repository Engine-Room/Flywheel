package dev.engine_room.flywheel.backend.compile.core;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;

import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import dev.engine_room.flywheel.backend.gl.shader.GlShader;
import net.minecraft.client.renderer.ShaderManager;

public class ProgramLinker {

	public ProgramLinker() {
	}

	public GlProgram link(List<GlShader> shaders, Consumer<GlProgram> preLink) {
		// this probably doesn't need caching
		return linkInternal(shaders, preLink).unwrap();
	}

	private LinkResult linkInternal(List<GlShader> shaders, Consumer<GlProgram> preLink) {
		int handle = GlStateManager.glCreateProgram();
		var out = new GlProgram(handle);

		for (GlShader shader : shaders) {
			GlStateManager.glAttachShader(handle, shader.handle());
		}

		preLink.accept(out);

		GlStateManager.glLinkProgram(handle);
		String log = GlStateManager.glGetProgramInfoLog(handle, ShaderManager.MAX_LOG_LENGTH);

		if (linkSuccessful(handle)) {
			return LinkResult.success(out, log);
		} else {
			out.delete();
			return LinkResult.failure(log);
		}
	}

	private static boolean linkSuccessful(int handle) {
		return GlStateManager.glGetProgrami(handle, GlConst.GL_LINK_STATUS) == GlConst.GL_TRUE;
	}

}
