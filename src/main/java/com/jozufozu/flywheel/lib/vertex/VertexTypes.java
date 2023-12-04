package com.jozufozu.flywheel.lib.vertex;

import org.jetbrains.annotations.ApiStatus;

public final class VertexTypes {
	public static final BlockVertex BLOCK = new BlockVertex();
	public static final PosTexNormalVertex POS_TEX_NORMAL = new PosTexNormalVertex();

	private VertexTypes() {
	}

	@ApiStatus.Internal
	public static void init() {
	}
}
