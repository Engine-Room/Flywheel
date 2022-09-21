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

	public static final BackendType OFF = SimpleBackendType.builder()
			.properName("Off")
			.shortName("off")
			.engineMessage(new TextComponent("Disabled Flywheel").withStyle(ChatFormatting.RED))
			.engineSupplier(() -> {
				throw new IllegalStateException("Cannot create engine when backend is off.");
			})
			.fallback(() -> BackendTypes.OFF)
			.supported(() -> true)
			.register();

	/**
	 * Use a thread pool to buffer instances in parallel on the CPU.
	 */
	public static final BackendType BATCHING = SimpleBackendType.builder()
			.properName("Parallel Batching")
			.shortName("batching")
			.engineMessage(new TextComponent("Using Batching Engine").withStyle(ChatFormatting.GREEN))
			.engineSupplier(BatchingEngine::new)
			.fallback(() -> BackendTypes.OFF)
			.supported(() -> !ShadersModHandler.isShaderPackInUse())
			.register();

	/**
	 * Use GPU instancing to render everything.
	 */
	public static final BackendType INSTANCING = SimpleBackendType.builder()
			.properName("GL33 Instanced Arrays")
			.shortName("instancing")
			.engineMessage(new TextComponent("Using Instancing Engine").withStyle(ChatFormatting.GREEN))
			.engineSupplier(() -> new InstancingEngine(Components.WORLD, 100 * 100))
			.fallback(() -> BackendTypes.BATCHING)
			.supported(() -> !ShadersModHandler.isShaderPackInUse() && GlCompat.getInstance()
					.instancedArraysSupported())
			.register();

	/**
	 * Use Compute shaders to cull instances.
	 */
	public static final BackendType INDIRECT = SimpleBackendType.builder()
			.properName("GL46 Compute Culling")
			.shortName("indirect")
			.engineMessage(new TextComponent("Using Indirect Engine").withStyle(ChatFormatting.GREEN))
			.engineSupplier(() -> new IndirectEngine(Components.WORLD, 100 * 100))
			.fallback(() -> BackendTypes.INSTANCING)
			.supported(() -> !ShadersModHandler.isShaderPackInUse() && GlCompat.getInstance()
					.supportsIndirect())
			.register();

	public static BackendType register(BackendType type) {
		BACKEND_TYPES.put(type.getShortName(), type);
		return type;
	}

	public static BackendType defaultForCurrentPC() {
		// TODO: Automatically select the best default config based on the user's driver
		return INDIRECT;
	}

	@Nullable
	public static BackendType getBackendType(String name) {
		return BACKEND_TYPES.get(name.toLowerCase(Locale.ROOT));
	}

	public static Collection<String> validNames() {
		return BACKEND_TYPES.keySet();
	}

	public static void init() {
		// noop
	}


}
