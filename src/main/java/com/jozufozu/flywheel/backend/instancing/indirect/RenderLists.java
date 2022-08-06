package com.jozufozu.flywheel.backend.instancing.indirect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.model.Mesh;

public class RenderLists {

	public final Map<RenderStage, Map<StructType<?>, IndirectList<?>>> renderLists = new EnumMap<>(RenderStage.class);

	public Collection<IndirectList<?>> get(RenderStage stage) {
		var renderList = renderLists.get(stage);
		if (renderList == null) {
			return Collections.emptyList();
		}
		return renderList.values();
	}

	@SuppressWarnings("unchecked")
	public <D extends InstancedPart> void add(RenderStage stage, StructType<D> type, Mesh mesh, IndirectInstancer<D> instancer) {
		var indirectList = (IndirectList<D>) renderLists.computeIfAbsent(stage, $ -> new HashMap<>())
				.computeIfAbsent(type, IndirectList::new);

		indirectList.add(mesh, instancer);
	}
}
