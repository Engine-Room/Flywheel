package com.jozufozu.flywheel.core.uniform;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.uniform.UniformProvider;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.mojang.blaze3d.systems.RenderSystem;

public class FogProvider implements UniformProvider {

	public static boolean FOG_UPDATE = true;

	@Override
	public int byteSize() {
		return 16 + 8 + 4;
	}

	@Override
	public FileResolution uniformShader() {
		return Components.Files.FOG_UNIFORMS;
	}

	@Override
	public ActiveUniformProvider activate(long ptr) {
		return new Active(ptr);
	}

	public static class Active implements ActiveUniformProvider {

		private final long ptr;

		public Active(long ptr) {
			this.ptr = ptr;
		}

		@Override
		public void delete() {
		}

		@Override
		public boolean poll() {
			if (!FOG_UPDATE) {
				return false;
			}

			var color = RenderSystem.getShaderFogColor();

			MemoryUtil.memPutFloat(ptr, color[0]);
			MemoryUtil.memPutFloat(ptr + 4, color[1]);
			MemoryUtil.memPutFloat(ptr + 8, color[2]);
			MemoryUtil.memPutFloat(ptr + 12, color[3]);
			MemoryUtil.memPutFloat(ptr + 16, RenderSystem.getShaderFogStart());
			MemoryUtil.memPutFloat(ptr + 20, RenderSystem.getShaderFogEnd());
			MemoryUtil.memPutInt(ptr + 24, RenderSystem.getShaderFogShape()
					.getIndex());

			FOG_UPDATE = false;

			return true;
		}
	}
}
