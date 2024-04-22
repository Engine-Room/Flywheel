package com.jozufozu.flywheel.api.vertex;

import com.jozufozu.flywheel.api.internal.FlwApiLink;
import com.mojang.blaze3d.vertex.VertexFormat;

public final class VertexViewProviderRegistry {
	private VertexViewProviderRegistry() {
	}

	public static VertexViewProvider getProvider(VertexFormat format) {
		return FlwApiLink.INSTANCE.getVertexViewProvider(format);
	}

	public static void setProvider(VertexFormat format, VertexViewProvider provider) {
		FlwApiLink.INSTANCE.setVertexViewProvider(format, provider);
	}
}
