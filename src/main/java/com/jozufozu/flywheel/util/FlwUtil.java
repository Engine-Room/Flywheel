package com.jozufozu.flywheel.util;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.FlywheelLevel;
import com.jozufozu.flywheel.api.backend.BackendManager;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.LevelAccessor;

public final class FlwUtil {
	public static boolean isGameActive() {
		return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
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

	public static <T> Set<T> createWeakHashSet() {
		return Collections.newSetFromMap(new WeakHashMap<>());
	}

	public static PoseStack copyPoseStack(PoseStack stack) {
		PoseStack copy = new PoseStack();
		copy.last().pose().load(stack.last().pose());
		copy.last().normal().load(stack.last().normal());
		return copy;
	}
}
