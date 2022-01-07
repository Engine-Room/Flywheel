package com.jozufozu.flywheel.core.shader.spec;

import com.jozufozu.flywheel.core.shader.gamestate.GameStateProvider;

public interface BooleanStateProvider extends GameStateProvider {

	boolean isTrue();

	@Override
	default Boolean getValue() {
		return isTrue();
	}
}
