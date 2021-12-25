package com.jozufozu.flywheel.fabric.helper;

import com.jozufozu.flywheel.fabric.mixin.VertexFormatAccessor;
import com.mojang.blaze3d.vertex.VertexFormat;

public final class VertexFormatHelper {
	public static int getOffset(VertexFormat self, int index) {
		return ((VertexFormatAccessor) self).getOffsets().getInt(index);
	}
}
