package com.jozufozu.flywheel.impl;

import com.jozufozu.flywheel.api.vertex.VertexListProvider;
import com.jozufozu.flywheel.extension.VertexFormatExtension;
import com.jozufozu.flywheel.impl.vertex.InferredVertexListProviderImpl;
import com.mojang.blaze3d.vertex.VertexFormat;

// TODO: Add freezing
public final class VertexListProviderRegistryImpl {
	public static VertexListProvider getProvider(VertexFormat format) {
		VertexFormatExtension extension = (VertexFormatExtension) format;
		VertexListProvider provider = extension.flywheel$getVertexListProvider();
		if (provider == null) {
			provider = new InferredVertexListProviderImpl(format);
			extension.flywheel$setVertexListProvider(provider);
		}
		return provider;
	}

	public static void setProvider(VertexFormat format, VertexListProvider provider) {
		((VertexFormatExtension) format).flywheel$setVertexListProvider(provider);
	}

	private VertexListProviderRegistryImpl() {
	}
}
