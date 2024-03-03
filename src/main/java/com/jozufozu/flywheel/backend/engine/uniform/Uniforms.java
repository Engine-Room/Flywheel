package com.jozufozu.flywheel.backend.engine.uniform;

import com.jozufozu.flywheel.api.event.ReloadLevelRendererEvent;
import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.config.DebugMode;

import org.jetbrains.annotations.Nullable;

public class Uniforms {
	public static boolean frustumPaused = false;
	public static boolean frustumCapture = false;
	private static @Nullable UniformBuffer<FrameUniforms> frame;
	private static @Nullable UniformBuffer<FogUniforms> fog;
	private static @Nullable UniformBuffer<OptionsUniforms> options;

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

	public static void bindForDraw() {
		bindFrame();
		bindFog();
		bindOptions();
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
	}
}
