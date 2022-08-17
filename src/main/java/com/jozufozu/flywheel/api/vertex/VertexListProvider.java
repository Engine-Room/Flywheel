package com.jozufozu.flywheel.api.vertex;

import com.jozufozu.flywheel.core.vertex.InferredVertexListProviderImpl;
import com.jozufozu.flywheel.extension.VertexFormatExtension;
import com.mojang.blaze3d.vertex.VertexFormat;

public interface VertexListProvider {
	ReusableVertexList createVertexList();

	static VertexListProvider get(VertexFormat format) {
		VertexFormatExtension extension = (VertexFormatExtension) format;
		VertexListProvider provider = extension.flywheel$getVertexListProvider();
		if (provider == null) {
			provider = new InferredVertexListProviderImpl(format);
			extension.flywheel$setVertexListProvider(provider);
		}
		return provider;
	}

	static void set(VertexFormat format, VertexListProvider provider) {
		((VertexFormatExtension) format).flywheel$setVertexListProvider(provider);
	}
}
