package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.api.vertex.VertexListProvider;
import com.mojang.blaze3d.vertex.VertexFormat;

public class DrawBufferSet {
	private final VertexFormat format;
	private final int stride;
	private final VertexListProvider provider;

	private final Map<RenderStage, DrawBuffer> buffers = new EnumMap<>(RenderStage.class);
	private final Set<RenderStage> activeStages = EnumSet.noneOf(RenderStage.class);
	private final Set<RenderStage> activeStagesView = Collections.unmodifiableSet(activeStages);

	@ApiStatus.Internal
	public DrawBufferSet(VertexFormat format) {
		this.format = format;
		stride = format.getVertexSize();
		provider = VertexListProvider.get(format);
	}

	public Set<RenderStage> getActiveStagesView() {
		return activeStagesView;
	}

	public DrawBuffer getBuffer(RenderStage stage) {
		activeStages.add(stage);
		return buffers.computeIfAbsent(stage, $ -> createBuffer());
	}

	@Nullable
	public DrawBuffer deactivateBuffer(RenderStage stage) {
		if (activeStages.remove(stage)) {
			return buffers.get(stage);
		}
		return null;
	}

	public void reset(RenderStage stage) {
		if (activeStages.remove(stage)) {
			buffers.get(stage).reset();
		}
	}

	public void reset() {
		for (RenderStage stage : activeStages) {
			buffers.get(stage).reset();
		}

		activeStages.clear();
	}

	private DrawBuffer createBuffer() {
		return new DrawBuffer(format, stride, provider);
	}
}
