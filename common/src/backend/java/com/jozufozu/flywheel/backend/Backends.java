package com.jozufozu.flywheel.backend;

import com.jozufozu.flywheel.api.Flywheel;
import com.jozufozu.flywheel.api.backend.Backend;
import com.jozufozu.flywheel.backend.compile.IndirectPrograms;
import com.jozufozu.flywheel.backend.compile.InstancingPrograms;
import com.jozufozu.flywheel.backend.engine.EngineImpl;
import com.jozufozu.flywheel.backend.engine.indirect.IndirectDrawManager;
import com.jozufozu.flywheel.backend.engine.instancing.InstancedDrawManager;
import com.jozufozu.flywheel.backend.gl.GlCompat;
import com.jozufozu.flywheel.lib.backend.SimpleBackend;
import com.jozufozu.flywheel.lib.util.ShadersModHandler;

public final class Backends {
	/**
	 * Use GPU instancing to render everything.
	 */
	public static final Backend INSTANCING = SimpleBackend.builder()
			.engineFactory(level -> new EngineImpl(new InstancedDrawManager(InstancingPrograms.get()), 256))
			.supported(() -> GlCompat.SUPPORTS_INSTANCING && InstancingPrograms.allLoaded() && !ShadersModHandler.isShaderPackInUse())
			.register(Flywheel.rl("instancing"));

	/**
	 * Use Compute shaders to cull instances.
	 */
	public static final Backend INDIRECT = SimpleBackend.builder()
			.engineFactory(level -> new EngineImpl(new IndirectDrawManager(IndirectPrograms.get()), 256))
			.fallback(() -> Backends.INSTANCING)
			.supported(() -> GlCompat.SUPPORTS_INDIRECT && IndirectPrograms.allLoaded() && !ShadersModHandler.isShaderPackInUse())
			.register(Flywheel.rl("indirect"));

	private Backends() {
	}

	public static void init() {
	}
}
