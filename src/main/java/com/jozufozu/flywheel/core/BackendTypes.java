package com.jozufozu.flywheel.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.backend.BackendType;
import com.jozufozu.flywheel.backend.ShadersModHandler;
import com.jozufozu.flywheel.backend.SimpleBackendType;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;
import com.jozufozu.flywheel.backend.instancing.batching.BatchingEngine;
import com.jozufozu.flywheel.backend.instancing.indirect.IndirectEngine;
import com.jozufozu.flywheel.backend.instancing.instancing.InstancingEngine;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;

public class BackendTypes {
	public static final Map<String, BackendType> BACKEND_TYPES = new HashMap<>();

	public static BackendType register(BackendType type) {
		BACKEND_TYPES.put(type.getShortName(), type);
		return type;
	}

	public static final BackendType OFF = SimpleBackendType.builder()
			.setProperName("Off")
			.setShortName("off")
			.setEngineMessage(new TextComponent("Disabled Flywheel").withStyle(ChatFormatting.RED))
			.setEngineSupplier(() -> {
				throw new IllegalStateException("Cannot create engine when backend is off.");
			})
			.setFallback(() -> BackendTypes.OFF)
			.supported(() -> true)
			.register();

	public static BackendType defaultForCurrentPC() {
		// TODO: Automatically select the best default config based on the user's driver
		return INDIRECT;
	}

	/**
	 * Use a thread pool to buffer instances in parallel on the CPU.
	 */
	public static final BackendType BATCHING = SimpleBackendType.builder()
			.setProperName("Parallel Batching")
			.setShortName("batching")
			.setEngineMessage(new TextComponent("Using Batching Engine").withStyle(ChatFormatting.GREEN))
			.setEngineSupplier(BatchingEngine::new)
			.setFallback(() -> BackendTypes.OFF)
			.supported(() -> !ShadersModHandler.isShaderPackInUse())
			.register();

	@Nullable
	public static BackendType getBackendType(String name) {
		return BACKEND_TYPES.get(name.toLowerCase(Locale.ROOT));
	}

	/**
	 * Use GPU instancing to render everything.
	 */
	public static final BackendType INSTANCING = SimpleBackendType.builder()
			.setProperName("GL33 Instanced Arrays")
			.setShortName("instancing")
			.setEngineMessage(new TextComponent("Using Instancing Engine").withStyle(ChatFormatting.GREEN))
			.setEngineSupplier(() -> new InstancingEngine(Components.WORLD, 100 * 100))
			.setFallback(() -> BackendTypes.BATCHING)
			.supported(() -> !ShadersModHandler.isShaderPackInUse() && GlCompat.getInstance()
					.instancedArraysSupported())
			.register();

	public static Collection<String> validNames() {
		return BACKEND_TYPES.keySet();
	}

	/**
	 * Use Compute shaders to cull instances.
	 */
	public static final BackendType INDIRECT = SimpleBackendType.builder()
			.setProperName("GL46 Compute Culling")
			.setShortName("indirect")
			.setEngineMessage(new TextComponent("Using Indirect Engine").withStyle(ChatFormatting.GREEN))
			.setEngineSupplier(() -> new IndirectEngine(Components.WORLD))
			.setFallback(() -> BackendTypes.INSTANCING)
			.supported(() -> !ShadersModHandler.isShaderPackInUse() && GlCompat.getInstance()
					.supportsIndirect())
			.register();

	public static void init() {
		// noop
	}


}
