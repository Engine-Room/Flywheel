package com.jozufozu.flywheel.backend;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.jozufozu.flywheel.api.FlywheelLevel;
import com.jozufozu.flywheel.api.backend.BackendType;
import com.jozufozu.flywheel.backend.task.ParallelTaskExecutor;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.lib.backend.BackendTypes;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class Backend {
	public static final Logger LOGGER = LogUtils.getLogger();

	public static final boolean DUMP_SHADER_SOURCE = System.getProperty("flw.dumpShaderSource") != null;

	private static BackendType TYPE;

	private static ParallelTaskExecutor EXECUTOR;

	private static final Loader LOADER = new Loader();

	/**
	 * Get the current Flywheel backend type.
	 */
	public static BackendType getBackendType() {
		return TYPE;
	}

	/**
	 * Get a thread pool for running Flywheel related work in parallel.
	 * @return A global Flywheel thread pool.
	 */
	public static ParallelTaskExecutor getTaskExecutor() {
		if (EXECUTOR == null) {
			EXECUTOR = new ParallelTaskExecutor("Flywheel");
			EXECUTOR.startWorkers();
		}

		return EXECUTOR;
	}

	/**
	 * Get a string describing the Flywheel backend. When there are eventually multiple backends
	 * (Meshlet, MDI, GL31 Draw Instanced are planned), this will name which one is in use.
	 */
	public static String getBackendDescriptor() {
		return TYPE == null ? "Uninitialized" : TYPE.getProperName();
	}

	public static void refresh() {
		TYPE = chooseEngine();
	}

	public static boolean isOn() {
		return TYPE != BackendTypes.OFF;
	}

	@Contract("null -> false")
	public static boolean canUseInstancing(@Nullable Level level) {
		return isOn() && isFlywheelLevel(level);
	}

	/**
	 * Used to avoid calling Flywheel functions on (fake) levels that don't specifically support it.
	 */
	public static boolean isFlywheelLevel(@Nullable LevelAccessor level) {
		if (level == null) return false;

		if (!level.isClientSide()) return false;

		if (level instanceof FlywheelLevel && ((FlywheelLevel) level).supportsFlywheel()) return true;

		return level == Minecraft.getInstance().level;
	}

	public static boolean isGameActive() {
		return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
	}

	public static void reloadWorldRenderers() {
		Minecraft.getInstance().levelRenderer.allChanged();
	}

	private static BackendType chooseEngine() {
		var preferred = FlwConfig.get()
				.getBackendType();
		if (preferred == null) {
			return BackendTypes.defaultForCurrentPC();
		}
		var actual = preferred.findFallback();

		if (preferred != actual) {
			LOGGER.warn("Flywheel backend fell back from '{}' to '{}'", preferred.getShortName(), actual.getShortName());
		}

		return actual;
	}

	public static void init() {
		// noop
	}

	private Backend() {
		throw new UnsupportedOperationException("Backend is a static class!");
	}

}
