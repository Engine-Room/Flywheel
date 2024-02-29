package com.jozufozu.flywheel.backend;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.backend.Backend;
import com.jozufozu.flywheel.backend.compile.IndirectPrograms;
import com.jozufozu.flywheel.backend.compile.InstancingPrograms;
import com.jozufozu.flywheel.backend.engine.indirect.IndirectEngine;
import com.jozufozu.flywheel.backend.engine.instancing.InstancingEngine;
import com.jozufozu.flywheel.backend.gl.GlCompat;
import com.jozufozu.flywheel.lib.backend.SimpleBackend;
import com.jozufozu.flywheel.lib.util.ShadersModHandler;

public final class Backends {
	/**
	 * Use GPU instancing to render everything.
	 */
	public static final Backend INSTANCING = SimpleBackend.builder()
			.engineFactory(level -> new InstancingEngine(InstancingPrograms.get(), 256))
			.supported(() -> !ShadersModHandler.isShaderPackInUse() && GlCompat.supportsInstancing() && InstancingPrograms.allLoaded())
			.register(Flywheel.rl("instancing"));

	/**
	 * Use Compute shaders to cull instances.
	 */
	public static final Backend INDIRECT = SimpleBackend.builder()
			.engineFactory(level -> new IndirectEngine(IndirectPrograms.get(), 256))
			.fallback(() -> Backends.INSTANCING)
			.supported(() -> !ShadersModHandler.isShaderPackInUse() && GlCompat.supportsIndirect() && IndirectPrograms.allLoaded())
			.register(Flywheel.rl("indirect"));

	private Backends() {
	}

	public static void init() {
	}
}
