package com.jozufozu.flywheel.core.shader.gamestate;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.core.shader.spec.BooleanStateProvider;

import net.minecraft.resources.ResourceLocation;

public class NormalDebugStateProvider implements BooleanStateProvider {

	public static final NormalDebugStateProvider INSTANCE = new NormalDebugStateProvider();
	public static final ResourceLocation NAME = Flywheel.rl("normal_debug");

	protected NormalDebugStateProvider() {

	}

	@Override
	public boolean isTrue() {
		return FlwConfig.get()
				.debugNormals();
	}

	@Override
	public ResourceLocation getID() {
		return NAME;
	}
}
