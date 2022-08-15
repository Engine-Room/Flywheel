package com.jozufozu.flywheel.core.uniform;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.uniform.UniformProvider;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.mojang.blaze3d.systems.RenderSystem;

public class FogProvider extends UniformProvider {

	@Override
	public int getActualByteSize() {
		return 16 + 8 + 4;
	}

	public void update() {
		if (ptr == MemoryUtil.NULL) {
			return;
		}

		var color = RenderSystem.getShaderFogColor();

		MemoryUtil.memPutFloat(ptr, color[0]);
		MemoryUtil.memPutFloat(ptr + 4, color[1]);
		MemoryUtil.memPutFloat(ptr + 8, color[2]);
		MemoryUtil.memPutFloat(ptr + 12, color[3]);
		MemoryUtil.memPutFloat(ptr + 16, RenderSystem.getShaderFogStart());
		MemoryUtil.memPutFloat(ptr + 20, RenderSystem.getShaderFogEnd());
		MemoryUtil.memPutInt(ptr + 24, RenderSystem.getShaderFogShape().getIndex());

		notifier.signalChanged();
	}

	@Override
	public FileResolution getUniformShader() {
		return Components.Files.FOG_UNIFORMS;
	}
}
