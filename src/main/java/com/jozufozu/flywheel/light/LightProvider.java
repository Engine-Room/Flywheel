package com.jozufozu.flywheel.light;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.LightType;

public interface LightProvider {
	int getLight(LightType type, int x, int y, int z);

	default int getPackedLight(int x, int y, int z) {
		return LightTexture.pack(getLight(LightType.BLOCK, x, y, z), getLight(LightType.SKY, x, y, z));
	}
}
