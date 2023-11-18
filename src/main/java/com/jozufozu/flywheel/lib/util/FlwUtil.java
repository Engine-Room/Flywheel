package com.jozufozu.flywheel.lib.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;

public final class FlwUtil {
	private FlwUtil() {
	}

	public static boolean isGameActive() {
		return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
	}

	public static <T> Set<T> createWeakHashSet() {
		return Collections.newSetFromMap(new WeakHashMap<>());
	}

	public static PoseStack copyPoseStack(PoseStack stack) {
		PoseStack copy = new PoseStack();
		copy.last()
				.pose()
				.load(stack.last()
						.pose());
		copy.last()
				.normal()
				.load(stack.last()
						.normal());
		return copy;
	}

	public static int[] initArray(int size, int fill) {
		var out = new int[size];
		Arrays.fill(out, fill);
		return out;
	}
}
