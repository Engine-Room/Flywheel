package com.jozufozu.flywheel.lib.math;

import net.minecraft.client.multiplayer.ClientLevel;

public interface DiffuseLightCalculator {
	DiffuseLightCalculator DEFAULT = RenderMath::diffuseLight;
	DiffuseLightCalculator NETHER = RenderMath::diffuseLightNether;

	static DiffuseLightCalculator forLevel(ClientLevel level) {
		return level.effects().constantAmbientLight() ? NETHER : DEFAULT;
	}

	float getDiffuse(float normalX, float normalY, float normalZ, boolean shaded);
}
