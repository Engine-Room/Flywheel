package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.EnumMap;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.api.vertex.VertexListProvider;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;

public class DrawBufferSet {
	private final RenderType renderType;
	private final VertexFormat format;
	private final int stride;
	private final VertexListProvider provider;

	private final Map<RenderStage, DrawBuffer> buffers = new EnumMap<>(RenderStage.class);

	@ApiStatus.Internal
	public DrawBufferSet(RenderType renderType) {
		this.renderType = renderType;
		format = renderType.format();
		stride = format.getVertexSize();
		provider = VertexListProvider.get(format);
	}

	public DrawBuffer getBuffer(RenderStage stage) {
		return buffers.computeIfAbsent(stage, $ -> new DrawBuffer(renderType, format, stride, provider));
	}
}
