package com.jozufozu.flywheel.util;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Stream;

import com.mojang.blaze3d.vertex.PoseStack;

public class FlwUtil {

	public static PoseStack copyPoseStack(PoseStack stack) {
		PoseStack copy = new PoseStack();
		copy.last().pose().load(stack.last().pose());
		copy.last().normal().load(stack.last().normal());
		return copy;
	}

	public static int numDigits(int number) {
		// cursed but allegedly the fastest algorithm, taken from https://www.baeldung.com/java-number-of-digits-in-int
		if (number < 100000) {
			if (number < 100) {
				if (number < 10) {
					return 1;
				} else {
					return 2;
				}
			} else {
				if (number < 1000) {
					return 3;
				} else {
					if (number < 10000) {
						return 4;
					} else {
						return 5;
					}
				}
			}
		} else {
			if (number < 10000000) {
				if (number < 1000000) {
					return 6;
				} else {
					return 7;
				}
			} else {
				if (number < 100000000) {
					return 8;
				} else {
					if (number < 1000000000) {
						return 9;
					} else {
						return 10;
					}
				}
			}
		}
	}

	public static <R> Stream<R> mapValues(Map<?, R> map) {
		return map.values()
				.stream();
	}

	public static <T> void noop(T object) {
		// noop
	}

	public static int align16(int numToRound) {
		return (numToRound + 16 - 1) & -16;
	}

	public static <T> Set<T> createWeakHashSet() {
		return Collections.newSetFromMap(new WeakHashMap<>());
	}
}
