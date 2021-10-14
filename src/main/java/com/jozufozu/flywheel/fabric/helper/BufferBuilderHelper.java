package com.jozufozu.flywheel.fabric.helper;

import com.jozufozu.flywheel.mixin.fabric.BufferBuilderAccessor;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;

public final class BufferBuilderHelper {
	public static VertexFormat getVertexFormat(BufferBuilder self) {
		return ((BufferBuilderAccessor) self).getFormat();
	}
}
