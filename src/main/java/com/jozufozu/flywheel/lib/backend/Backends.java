package com.jozufozu.flywheel.lib.backend;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.backend.Backend;
import com.jozufozu.flywheel.backend.engine.batching.BatchingEngine;
import com.jozufozu.flywheel.backend.engine.indirect.IndirectEngine;
import com.jozufozu.flywheel.backend.engine.instancing.InstancingEngine;
import com.jozufozu.flywheel.gl.versioned.GlCompat;
import com.jozufozu.flywheel.lib.context.Contexts;
import com.jozufozu.flywheel.lib.pipeline.Pipelines;
import com.jozufozu.flywheel.lib.util.ShadersModHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;

public class Backends {
	public static final Backend OFF = SimpleBackend.builder()
			.properName("Off")
			.engineMessage(new TextComponent("Disabled Flywheel").withStyle(ChatFormatting.RED))
			.engineSupplier(() -> {
				throw new IllegalStateException("Cannot create engine when backend is off.");
			})
			.fallback(() -> Backends.OFF)
			.supported(() -> true)
			.register(Flywheel.rl("off"));

	/**
	 * Use a thread pool to buffer instances in parallel on the CPU.
	 */
	public static final Backend BATCHING = SimpleBackend.builder()
			.properName("Parallel Batching")
			.engineMessage(new TextComponent("Using Batching Engine").withStyle(ChatFormatting.GREEN))
			.engineSupplier(BatchingEngine::new)
			.fallback(() -> Backends.OFF)
			.supported(() -> !ShadersModHandler.isShaderPackInUse())
			.register(Flywheel.rl("batching"));

	/**
	 * Use GPU instancing to render everything.
	 */
	public static final Backend INSTANCING = SimpleBackend.builder()
			.properName("GL33 Instanced Arrays")
			.engineMessage(new TextComponent("Using Instancing Engine").withStyle(ChatFormatting.GREEN))
			.engineSupplier(() -> new InstancingEngine(Contexts.WORLD, 100 * 100))
			.fallback(() -> Backends.BATCHING)
			.supported(() -> !ShadersModHandler.isShaderPackInUse() && GlCompat.getInstance()
					.instancedArraysSupported())
			.pipelineShader(Pipelines.INSTANCED_ARRAYS)
			.register(Flywheel.rl("instancing"));

	/**
	 * Use Compute shaders to cull instances.
	 */
	public static final Backend INDIRECT = SimpleBackend.builder()
			.properName("GL46 Compute Culling")
			.engineMessage(new TextComponent("Using Indirect Engine").withStyle(ChatFormatting.GREEN))
			.engineSupplier(() -> new IndirectEngine(Contexts.WORLD, 100 * 100))
			.fallback(() -> Backends.INSTANCING)
			.supported(() -> !ShadersModHandler.isShaderPackInUse() && GlCompat.getInstance()
					.supportsIndirect())
			.pipelineShader(Pipelines.INDIRECT)
			.register(Flywheel.rl("indirect"));

	public static void init() {
	}
}
