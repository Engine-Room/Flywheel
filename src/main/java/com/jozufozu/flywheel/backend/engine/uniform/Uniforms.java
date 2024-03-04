package com.jozufozu.flywheel.backend.engine.uniform;

import com.jozufozu.flywheel.api.event.ReloadLevelRendererEvent;
import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.config.DebugMode;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

public class Uniforms {
	public static boolean frustumPaused = false;
	public static boolean frustumCapture = false;
	private static @Nullable UniformBuffer<FrameUniforms> frame;
	private static @Nullable UniformBuffer<FogUniforms> fog;
	private static @Nullable UniformBuffer<OptionsUniforms> options;
	private static @Nullable UniformBuffer<PlayerUniforms> player;

	public static UniformBuffer<FrameUniforms> frame() {
		if (frame == null) {
			frame = new UniformBuffer<>(0, new FrameUniforms());
		}
		return frame;
	}

	public static UniformBuffer<FogUniforms> fog() {
		if (fog == null) {
			fog = new UniformBuffer<>(1, new FogUniforms());
		}
		return fog;
	}

	public static UniformBuffer<OptionsUniforms> options() {
		if (options == null) {
			options = new UniformBuffer<>(2, new OptionsUniforms());
		}
		return options;
	}

	public static UniformBuffer<PlayerUniforms> player() {
		if (player == null) {
			player = new UniformBuffer<>(2, new PlayerUniforms());
		}
		return player;
	}

	public static void bindForDraw() {
		bindFrame();
		bindFog();
		bindOptions();
		bindPlayer();
	}

	public static void bindFrame() {
		if (frame != null) {
			frame.bind();
		}
	}

	public static void bindFog() {
		if (fog != null) {
			fog.bind();
		}
	}

	public static void bindOptions() {
		if (options != null) {
			options.bind();
		}
	}

	public static void bindPlayer() {
		if (player != null) {
			player.bind();
		}
	}

	public static void onFogUpdate() {
		try (var restoreState = GlStateTracker.getRestoreState()) {
			fog().update();
		}
	}

	public static void updateContext(RenderContext ctx) {
		var ubo = frame();
		ubo.provider.setContext(ctx);
		ubo.update();

		options();

		var player = player();
		player.provider.setContext(ctx);
		player.update();
	}

	public static void setDebugMode(DebugMode mode) {
		frame().provider.debugMode = mode.ordinal();
	}

	public static void onReloadLevelRenderer(ReloadLevelRendererEvent event) {
		if (frame != null) {
			frame.delete();
			frame = null;
		}

		if (fog != null) {
			fog.delete();
			fog = null;
		}

		if (options != null) {
			options.delete();
			options = null;
		}

		if (player != null) {
			player.delete();
			player = null;
		}
	}

	static long writeVec3(long ptr, float camX, float camY, float camZ) {
		MemoryUtil.memPutFloat(ptr, camX);
		MemoryUtil.memPutFloat(ptr + 4, camY);
		MemoryUtil.memPutFloat(ptr + 8, camZ);
		MemoryUtil.memPutFloat(ptr + 12, 0f); // empty component of vec4 because we don't trust std140
		return ptr + 16;
	}

	static long writeVec2(long ptr, float camX, float camY) {
		MemoryUtil.memPutFloat(ptr, camX);
		MemoryUtil.memPutFloat(ptr + 4, camY);
		return ptr + 8;
	}
}
