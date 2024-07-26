package dev.engine_room.flywheel.lib.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class FlwUtil {
	private FlwUtil() {
	}

	public static int[] initArray(int size, int fill) {
		var out = new int[size];
		Arrays.fill(out, fill);
		return out;
	}

	public static <T> Set<T> createWeakHashSet() {
		return Collections.newSetFromMap(new WeakHashMap<>());
	}
}
