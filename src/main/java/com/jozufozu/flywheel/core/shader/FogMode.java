package com.jozufozu.flywheel.core.shader;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.shader.extension.IExtensionInstance;

import net.minecraft.resources.ResourceLocation;

public abstract class FogMode {

	public static class Linear implements IExtensionInstance {

		public static final ResourceLocation NAME = new ResourceLocation(Flywheel.ID, "fog_linear");

		private final int uFogColor;
		private final int uFogRange;

		public Linear(GlProgram program) {
			this.uFogColor = program.getUniformLocation("uFogColor");
			this.uFogRange = program.getUniformLocation("uFogRange");
		}

		@Override
		public void bind() {
			GL20.glUniform2f(uFogRange, GlFog.getFogStart(), GlFog.getFogEnd());
			GL20.glUniform4fv(uFogColor, GlFog.FOG_COLOR);
		}

		@Override
		public ResourceLocation name() {
			return NAME;
		}
	}

	public static class Exp2 implements IExtensionInstance {

		public static final ResourceLocation NAME = new ResourceLocation(Flywheel.ID, "fog_exp2");

		private final int uFogColor;
		private final int uFogDensity;

		public Exp2(GlProgram program) {
			this.uFogColor = program.getUniformLocation("uFogColor");
			this.uFogDensity = program.getUniformLocation("uFogDensity");
		}

		@Override
		public void bind() {
			GL20.glUniform1f(uFogDensity, GlFog.getFogDensity());
			GL20.glUniform4fv(uFogColor, GlFog.FOG_COLOR);
		}

		@Override
		public ResourceLocation name() {
			return NAME;
		}
	}
}
