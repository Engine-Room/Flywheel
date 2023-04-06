package com.jozufozu.flywheel.backend;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.FlywheelLevel;
import com.jozufozu.flywheel.api.backend.BackendManager;
import com.jozufozu.flywheel.backend.task.ParallelTaskExecutor;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.LevelAccessor;

public class BackendUtil {
	private static ParallelTaskExecutor executor;

	/**
	 * Get a thread pool for running Flywheel related work in parallel.
	 * @return A global Flywheel thread pool.
	 */
	public static ParallelTaskExecutor getTaskExecutor() {
		if (executor == null) {
			executor = new ParallelTaskExecutor("Flywheel");
			executor.startWorkers();
		}

		return executor;
	}

	@Contract("null -> false")
	public static boolean canUseInstancing(@Nullable LevelAccessor level) {
		return BackendManager.isOn() && isFlywheelLevel(level);
	}

	/**
	 * Used to avoid calling Flywheel functions on (fake) levels that don't specifically support it.
	 */
	public static boolean isFlywheelLevel(@Nullable LevelAccessor level) {
		if (level == null) {
			return false;
		}

		if (!level.isClientSide()) {
			return false;
		}

		if (level instanceof FlywheelLevel flywheelLevel && flywheelLevel.supportsFlywheel()) {
			return true;
		}

		return level == Minecraft.getInstance().level;
	}

	public static boolean isGameActive() {
		return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
	}
}
