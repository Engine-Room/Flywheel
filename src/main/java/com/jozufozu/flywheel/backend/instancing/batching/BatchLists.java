package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.RenderType;

public class BatchLists {

	public final Map<RenderType, List<TransformSet<?>>> renderLists = new HashMap<>();

	public void add(TransformSet<?> set) {
		renderLists.computeIfAbsent(set.material.getBatchingRenderType(), k -> new ArrayList<>())
				.add(set);
	}
}
