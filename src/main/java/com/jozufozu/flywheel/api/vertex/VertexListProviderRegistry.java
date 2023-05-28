package com.jozufozu.flywheel.api.vertex;

import com.jozufozu.flywheel.impl.vertex.VertexListProviderRegistryImpl;
import com.mojang.blaze3d.vertex.VertexFormat;

public final class VertexListProviderRegistry {
	public static VertexListProvider getProvider(VertexFormat format) {
		return VertexListProviderRegistryImpl.getProvider(format);
	}

	public static void setProvider(VertexFormat format, VertexListProvider provider) {
		VertexListProviderRegistryImpl.setProvider(format, provider);
	}

	private VertexListProviderRegistry() {
	}
}
