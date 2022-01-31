package com.jozufozu.flywheel.backend;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jozufozu.flywheel.api.FlywheelWorld;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.config.FlwEngine;
import com.jozufozu.flywheel.core.shader.ProgramSpec;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class Backend {
	public static final Logger LOGGER = LogManager.getLogger(Backend.class);

	private static FlwEngine engine;

	public static GlCompat compat;

	private static final Loader loader = new Loader();

	public static FlwEngine getEngine() {
		return engine;
	}

	/**
	 * Get a string describing the Flywheel backend. When there are eventually multiple backends
	 * (Meshlet, MDI, GL31 Draw Instanced are planned), this will name which one is in use.
	 */
	public static String getBackendDescriptor() {
		return engine == null ? "" : engine.getProperName();
	}

	@Nullable
	public static ProgramSpec getSpec(ResourceLocation name) {
		return loader.get(name);
	}

	public static void refresh() {
		OptifineHandler.refresh();

		compat = new GlCompat();

		engine = chooseEngine(compat);
	}

	public static boolean isOn() {
		return engine != FlwEngine.OFF;
	}

	public static boolean canUseInstancing(@Nullable Level world) {
		return isOn() && isFlywheelWorld(world);
	}

	/**
	 * Used to avoid calling Flywheel functions on (fake) worlds that don't specifically support it.
	 */
	public static boolean isFlywheelWorld(@Nullable LevelAccessor world) {
		if (world == null) return false;

		if (!world.isClientSide()) return false;

		if (world instanceof FlywheelWorld && ((FlywheelWorld) world).supportsFlywheel()) return true;

		return world == Minecraft.getInstance().level;
	}

	public static boolean isGameActive() {
		return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
	}

	public static void reloadWorldRenderers() {
		RenderWork.enqueue(Minecraft.getInstance().levelRenderer::allChanged);
	}

	private static FlwEngine chooseEngine(GlCompat compat) {
		FlwEngine preferredChoice = FlwConfig.get()
				.getEngine();

		boolean usingShaders = OptifineHandler.usingShaders();
		boolean canUseEngine = switch (preferredChoice) {
			case OFF -> true;
			case BATCHING -> !usingShaders;
			case INSTANCING -> !usingShaders && compat.instancedArraysSupported();
		};

		return canUseEngine ? preferredChoice : FlwEngine.OFF;
	}

	public static void init() {
		// noop
	}

	private Backend() {
		throw new UnsupportedOperationException("Backend is a static class!");
	}
}
