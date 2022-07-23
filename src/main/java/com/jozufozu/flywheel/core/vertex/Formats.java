package com.jozufozu.flywheel.core.vertex;

import com.jozufozu.flywheel.core.ComponentRegistry;

public class Formats {
	public static final BlockVertex BLOCK = ComponentRegistry.register(new BlockVertex());
	public static final PosTexNormalVertex POS_TEX_NORMAL = ComponentRegistry.register(new PosTexNormalVertex());

	public static void init() {
		// noop
	}
}
