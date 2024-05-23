package dev.engine_room.flywheel.impl.vertex;

import com.mojang.blaze3d.vertex.VertexFormat;

import dev.engine_room.flywheel.api.vertex.VertexViewProvider;
import dev.engine_room.flywheel.impl.extension.VertexFormatExtension;

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
