package com.jozufozu.flywheel.core.shader;

import com.jozufozu.flywheel.config.FlwConfig;

public enum NormalDebugStateProvider implements GameStateProvider {
	INSTANCE;

	@Override
	public boolean isTrue() {
		return FlwConfig.get()
				.debugNormals();
	}

	@Override
	public void alterConstants(ShaderConstants constants) {
		constants.define("DEBUG_NORMAL");
	}
}
