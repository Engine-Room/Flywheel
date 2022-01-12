package com.jozufozu.flywheel.core.shader;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.core.compile.ShaderConstants;

import net.minecraft.resources.ResourceLocation;

public class NormalDebugStateProvider implements GameStateProvider {

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

	@Override
	public void alterConstants(ShaderConstants constants) {
		constants.define("DEBUG_NORMAL");
	}
}
