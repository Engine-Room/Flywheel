package com.jozufozu.flywheel.backend.instancing.batching;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.client.renderer.RenderType;

public class BatchLists {
	public final Multimap<RenderType, TransformSet<?>> renderLists = ArrayListMultimap.create();

	public void add(TransformSet<?> set) {
		renderLists.put(set.getMaterial().getBatchingRenderType(), set);
	}
}
