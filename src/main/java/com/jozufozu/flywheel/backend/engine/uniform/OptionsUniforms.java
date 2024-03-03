package com.jozufozu.flywheel.backend.engine.uniform;

import org.lwjgl.system.MemoryUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

public class OptionsUniforms implements UniformProvider {
	public static final int SIZE = 4 * 14;

	@Override
	public int byteSize() {
		return SIZE;
	}

	@Override
	public void write(long ptr) {
		Options options = Minecraft.getInstance().options;

		MemoryUtil.memPutFloat(ptr, options.gamma().get().floatValue());
		ptr += 4;

		MemoryUtil.memPutInt(ptr, options.fov().get());
		ptr += 4;

		MemoryUtil.memPutFloat(ptr, options.screenEffectScale().get().floatValue());
		ptr += 4;

		MemoryUtil.memPutFloat(ptr, options.glintSpeed().get().floatValue());
		ptr += 4;

		MemoryUtil.memPutFloat(ptr, options.glintStrength().get().floatValue());
		ptr += 4;

		MemoryUtil.memPutInt(ptr, options.biomeBlendRadius().get());
		ptr += 4;

		MemoryUtil.memPutInt(ptr, options.ambientOcclusion().get() ? 1 : 0);
		ptr += 4;

		MemoryUtil.memPutInt(ptr, options.bobView().get() ? 1 : 0);
		ptr += 4;

		MemoryUtil.memPutInt(ptr, options.highContrast().get() ? 1 : 0);
		ptr += 4;

		MemoryUtil.memPutFloat(ptr, options.textBackgroundOpacity().get().floatValue());
		ptr += 4;

		MemoryUtil.memPutInt(ptr, options.backgroundForChatOnly().get() ? 1 : 0);
		ptr += 4;

		MemoryUtil.memPutFloat(ptr, options.darknessEffectScale().get().floatValue());
		ptr += 4;

		MemoryUtil.memPutFloat(ptr, options.damageTiltStrength().get().floatValue());
		ptr += 4;

		MemoryUtil.memPutInt(ptr, options.hideLightningFlash().get() ? 1 : 0);
	}
}
