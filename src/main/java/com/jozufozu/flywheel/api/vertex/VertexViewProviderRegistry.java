package com.jozufozu.flywheel.api.vertex;

import com.jozufozu.flywheel.impl.vertex.VertexListProviderRegistryImpl;
import com.mojang.blaze3d.vertex.VertexFormat;

public final class VertexViewProviderRegistry {
	public static VertexViewProvider getProvider(VertexFormat format) {
		return VertexListProviderRegistryImpl.getProvider(format);
	}

	public static void setProvider(VertexFormat format, VertexViewProvider provider) {
		VertexListProviderRegistryImpl.setProvider(format, provider);
	}

	private VertexViewProviderRegistry() {
	}
}
