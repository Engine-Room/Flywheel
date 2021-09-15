package com.jozufozu.flywheel.light;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.level.LightLayer;

public interface LightProvider {
	int getLight(LightLayer type, int x, int y, int z);

	default int getPackedLight(int x, int y, int z) {
		return LightTexture.pack(getLight(LightLayer.BLOCK, x, y, z), getLight(LightLayer.SKY, x, y, z));
	}
}
