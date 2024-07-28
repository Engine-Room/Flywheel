package dev.engine_room.flywheel.backend.engine.uniform;

import dev.engine_room.flywheel.api.RenderContext;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;

public final class Uniforms {
	public static final int FRAME_INDEX = 0;
	public static final int FOG_INDEX = 1;
	public static final int OPTIONS_INDEX = 2;
	public static final int PLAYER_INDEX = 3;
	public static final int LEVEL_INDEX = 4;

	public static final String FRAME_BLOCK_NAME = "_FlwFrameUniforms";
	public static final String FOG_BLOCK_NAME = "_FlwFogUniforms";
	public static final String OPTIONS_BLOCK_NAME = "_FlwOptionsUniforms";
	public static final String PLAYER_BLOCK_NAME = "_FlwPlayerUniforms";
	public static final String LEVEL_BLOCK_NAME = "_FlwLevelUniforms";

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

	public static void setUniformBlockBindings(GlProgram program) {
		program.setUniformBlockBinding(FRAME_BLOCK_NAME, FRAME_INDEX);
		program.setUniformBlockBinding(FOG_BLOCK_NAME, FOG_INDEX);
		program.setUniformBlockBinding(OPTIONS_BLOCK_NAME, OPTIONS_INDEX);
		program.setUniformBlockBinding(PLAYER_BLOCK_NAME, PLAYER_INDEX);
		program.setUniformBlockBinding(LEVEL_BLOCK_NAME, LEVEL_INDEX);
	}

	public static void onReloadLevelRenderer() {
		deleteAll();
	}
}
