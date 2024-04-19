package com.jozufozu.flywheel.backend.engine.uniform;

import com.jozufozu.flywheel.api.event.RenderContext;

public final class Uniforms {
	public static final int FRAME_INDEX = 0;
	public static final int FOG_INDEX = 1;
	public static final int OPTIONS_INDEX = 2;
	public static final int PLAYER_INDEX = 3;
	public static final int LEVEL_INDEX = 4;

	private static final UniformBuffer[] ALL_BUFFERS = { FrameUniforms.BUFFER, FogUniforms.BUFFER, OptionsUniforms.BUFFER, PlayerUniforms.BUFFER, LevelUniforms.BUFFER };

	private Uniforms() {
	}

	public static void update(RenderContext context) {
		FrameUniforms.update(context);
		PlayerUniforms.update(context);
		LevelUniforms.update(context);
	}

	public static void bindAll() {
		for (UniformBuffer buffer : ALL_BUFFERS) {
			buffer.bind();
		}
	}

	private static void deleteAll() {
		for (UniformBuffer buffer : ALL_BUFFERS) {
			buffer.delete();
		}
	}

	public static void onReloadLevelRenderer() {
		deleteAll();
	}
}
