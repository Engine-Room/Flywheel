package com.jozufozu.flywheel.backend.engine.uniform;

import com.jozufozu.flywheel.api.event.ReloadLevelRendererEvent;
import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.config.DebugMode;

public class Uniforms {
	public static boolean frustumPaused = false;
	public static boolean frustumCapture = false;
	private static UniformBuffer<FrameUniforms> frame;
	private static UniformBuffer<FogUniforms> fog;

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

	public static void bindForDraw() {
		bindFrame();
		bindFog();
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

	public static void onFogUpdate() {
		try (var restoreState = GlStateTracker.getRestoreState()) {
			fog().update();
		}
	}

	public static void updateContext(RenderContext ctx) {
		var ubo = frame();
		ubo.provider.setContext(ctx);
		ubo.update();
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
	}
}
