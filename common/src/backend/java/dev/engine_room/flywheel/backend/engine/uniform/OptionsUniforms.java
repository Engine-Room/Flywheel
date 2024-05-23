package dev.engine_room.flywheel.backend.engine.uniform;

import net.minecraft.client.Options;

public final class OptionsUniforms extends UniformWriter {
	private static final int SIZE = 4 * 14;
	static final UniformBuffer BUFFER = new UniformBuffer(Uniforms.OPTIONS_INDEX, SIZE);

	public static void update(Options options) {
		long ptr = BUFFER.ptr();

		ptr = writeFloat(ptr, options.gamma().get().floatValue());
		ptr = writeInt(ptr, options.fov().get());
		ptr = writeFloat(ptr, options.screenEffectScale().get().floatValue());
		ptr = writeFloat(ptr, options.glintSpeed().get().floatValue());
		ptr = writeFloat(ptr, options.glintStrength().get().floatValue());
		ptr = writeInt(ptr, options.biomeBlendRadius().get());
		ptr = writeInt(ptr, options.ambientOcclusion().get() ? 1 : 0);
		ptr = writeInt(ptr, options.bobView().get() ? 1 : 0);
		ptr = writeInt(ptr, options.highContrast().get() ? 1 : 0);
		ptr = writeFloat(ptr, options.textBackgroundOpacity().get().floatValue());
		ptr = writeInt(ptr, options.backgroundForChatOnly().get() ? 1 : 0);
		ptr = writeFloat(ptr, options.darknessEffectScale().get().floatValue());
		ptr = writeFloat(ptr, options.damageTiltStrength().get().floatValue());
		ptr = writeInt(ptr, options.hideLightningFlash().get() ? 1 : 0);

		BUFFER.markDirty();
	}
}
