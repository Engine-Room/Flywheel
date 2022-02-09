package com.jozufozu.flywheel.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.client.model.pipeline.LightUtil;

public interface DiffuseLightCalculator {
	DiffuseLightCalculator DEFAULT = LightUtil::diffuseLight;
	DiffuseLightCalculator NETHER = RenderMath::diffuseLightNether;

	static DiffuseLightCalculator forCurrentLevel() {
		return forLevel(Minecraft.getInstance().level);
	}

	static DiffuseLightCalculator forLevel(ClientLevel level) {
		return level.effects().constantAmbientLight() ? NETHER : DEFAULT;
	}

	float getDiffuse(float normalX, float normalY, float normalZ);
}
