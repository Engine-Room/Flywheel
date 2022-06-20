package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.minecraft.client.renderer.RenderType;

public class RenderLists {

	private Map<RenderType, ListMultimap<ShaderState, DrawCall>> renderLists = new HashMap<>();
	public final Set<RenderType> layersToProcess = new HashSet<>();

	public ListMultimap<ShaderState, DrawCall> get(RenderType type) {
		return renderLists.computeIfAbsent(type, k -> ArrayListMultimap.create());
	}

	public void add(ShaderState shaderState, DrawCall layer) {
		RenderType renderType = shaderState.material()
				.renderType();

		get(renderType).put(shaderState, layer);
	}

	public void prepare() {
		layersToProcess.clear();

		layersToProcess.addAll(renderLists.keySet());
	}

	public Iterable<? extends RenderType> drainLayers() {
		var out = new HashSet<>(layersToProcess);
		layersToProcess.clear();
		return out;
	}

	/**
	 * Check and mark a layer as processed.
	 * @param type The layer to check.
	 * @return {@code true} if the layer should be processed.
	 */
	public boolean process(RenderType type) {
		return layersToProcess.remove(type);
	}
}
