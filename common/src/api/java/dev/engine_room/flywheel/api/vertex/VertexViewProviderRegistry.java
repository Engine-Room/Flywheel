package dev.engine_room.flywheel.api.vertex;

import com.mojang.blaze3d.vertex.VertexFormat;

import dev.engine_room.flywheel.api.internal.FlwApiLink;

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
