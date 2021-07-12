package com.jozufozu.flywheel.util;

import org.apache.commons.lang3.mutable.MutableBoolean;

public final class VirtualRenderingStateManager {
	private static final ThreadLocal<MutableBoolean> STATE = ThreadLocal.withInitial(MutableBoolean::new);

	public static boolean getState() {
		return STATE.get().booleanValue();
	}

	public static void setState(boolean state) {
		STATE.get().setValue(state);
	}

	public static void runVirtually(Runnable runnable) {
		setState(true);
		runnable.run();
		setState(false);
	}
}
