package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.api.material.Material;

public class RenderLists {

	public final Map<RenderStage, ListMultimap<ShaderState, DrawCall>> renderLists = new EnumMap<>(RenderStage.class);

	public ListMultimap<ShaderState, DrawCall> get(RenderStage stage) {
		var renderList = renderLists.get(stage);
		if (renderList == null) {
			return ImmutableListMultimap.of();
		}
		return renderList;
	}

	public void add(ShaderState shaderState, DrawCall layer) {
		Material material = shaderState.material();

		renderLists.computeIfAbsent(material.getRenderStage(), k -> ArrayListMultimap.create())
			.put(shaderState, layer);
	}

	public boolean isEmpty() {
		return renderLists.isEmpty();
	}

	public Collection<ListMultimap<ShaderState, DrawCall>> getAll() {
		return renderLists.values();
	}
}
