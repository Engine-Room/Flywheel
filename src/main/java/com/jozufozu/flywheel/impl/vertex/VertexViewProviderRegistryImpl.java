package com.jozufozu.flywheel.impl.vertex;

import com.jozufozu.flywheel.api.vertex.VertexViewProvider;
import com.jozufozu.flywheel.impl.extension.VertexFormatExtension;
import com.mojang.blaze3d.vertex.VertexFormat;

// TODO: Add freezing
public final class VertexViewProviderRegistryImpl {
	private VertexViewProviderRegistryImpl() {
	}

	public static VertexViewProvider getProvider(VertexFormat format) {
		VertexFormatExtension extension = (VertexFormatExtension) format;
		VertexViewProvider provider = extension.flywheel$getVertexViewProvider();
		if (provider == null) {
			provider = new InferredVertexViewProvider(format);
			extension.flywheel$setVertexViewProvider(provider);
		}
		return provider;
	}

	public static void setProvider(VertexFormat format, VertexViewProvider provider) {
		((VertexFormatExtension) format).flywheel$setVertexViewProvider(provider);
	}
}
