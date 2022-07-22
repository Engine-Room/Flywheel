package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.api.material.Material;

import net.minecraft.client.renderer.RenderType;

public class RenderLists {

	private final Map<RenderStage, Map<RenderType, ListMultimap<ShaderState, DrawCall>>> renderLists = new EnumMap<>(RenderStage.class);
	public final Set<RenderStage> stagesToProcess = EnumSet.noneOf(RenderStage.class);

	public Map<RenderType, ListMultimap<ShaderState, DrawCall>> get(RenderStage stage) {
		var renderList = renderLists.get(stage);
		if (renderList == null) {
			return Collections.emptyMap();
		}
		return renderList;
	}

	public void add(ShaderState shaderState, DrawCall layer) {
		Material material = shaderState.material();

		renderLists
			.computeIfAbsent(material.getRenderStage(), k -> new HashMap<>())
			.computeIfAbsent(material.getRenderType(), k -> ArrayListMultimap.create())
			.put(shaderState, layer);
	}

	public void prepare() {
		stagesToProcess.clear();

		stagesToProcess.addAll(renderLists.keySet());
	}

	/**
	 * Check and mark a stage as processed.
	 * @param stage The stage to check.
	 * @return {@code true} if the stage should be processed.
	 */
	public boolean process(RenderStage stage) {
		return stagesToProcess.remove(stage);
	}

	public boolean isEmpty() {
		return stagesToProcess.isEmpty();
	}
}
