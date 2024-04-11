package com.jozufozu.flywheel.backend.engine.uniform;

import com.mojang.blaze3d.systems.RenderSystem;

public final class FogUniforms extends UniformWriter {
	private static final int SIZE = 4 * 7;
	static final UniformBuffer BUFFER = new UniformBuffer(Uniforms.FOG_INDEX, SIZE);

	public static void update() {
		long ptr = BUFFER.ptr();

		var color = RenderSystem.getShaderFogColor();

		ptr = writeFloat(ptr, color[0]);
		ptr = writeFloat(ptr + 4, color[1]);
		ptr = writeFloat(ptr + 8, color[2]);
		ptr = writeFloat(ptr + 12, color[3]);
		ptr = writeFloat(ptr + 16, RenderSystem.getShaderFogStart());
		ptr = writeFloat(ptr + 20, RenderSystem.getShaderFogEnd());
		ptr = writeInt(ptr + 24, RenderSystem.getShaderFogShape()
				.getIndex());

		BUFFER.markDirty();
	}
}
