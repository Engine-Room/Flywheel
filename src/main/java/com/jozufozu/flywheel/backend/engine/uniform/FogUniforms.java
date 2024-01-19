package com.jozufozu.flywheel.backend.engine.uniform;

import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.systems.RenderSystem;

public class FogUniforms implements UniformProvider {
	public static final int SIZE = 28;

	public int byteSize() {
		return SIZE;
	}

	@Override
	public void write(long ptr) {
		var color = RenderSystem.getShaderFogColor();

		MemoryUtil.memPutFloat(ptr, color[0]);
		MemoryUtil.memPutFloat(ptr + 4, color[1]);
		MemoryUtil.memPutFloat(ptr + 8, color[2]);
		MemoryUtil.memPutFloat(ptr + 12, color[3]);
		MemoryUtil.memPutFloat(ptr + 16, RenderSystem.getShaderFogStart());
		MemoryUtil.memPutFloat(ptr + 20, RenderSystem.getShaderFogEnd());
		MemoryUtil.memPutInt(ptr + 24, RenderSystem.getShaderFogShape()
				.getIndex());
	}
}
