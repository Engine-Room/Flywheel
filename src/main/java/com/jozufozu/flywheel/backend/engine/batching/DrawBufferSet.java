package com.jozufozu.flywheel.backend.engine.batching;

import java.util.EnumMap;
import java.util.Map;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.vertex.VertexViewProvider;
import com.jozufozu.flywheel.api.vertex.VertexViewProviderRegistry;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;

public class DrawBufferSet {
	private final RenderType renderType;
	private final VertexFormat format;
	private final boolean sortOnUpload;
	private final int stride;
	private final VertexViewProvider provider;
	private final Map<RenderStage, DrawBuffer> buffers = new EnumMap<>(RenderStage.class);

	public DrawBufferSet(RenderType renderType, boolean sortOnUpload) {
		this.renderType = renderType;
		this.sortOnUpload = sortOnUpload;
		format = renderType.format();
		stride = format.getVertexSize();
		provider = VertexViewProviderRegistry.getProvider(format);
	}

	public DrawBuffer getBuffer(RenderStage stage) {
		return buffers.computeIfAbsent(stage, renderStage -> new DrawBuffer(renderType, format, stride, sortOnUpload, provider));
	}
}
