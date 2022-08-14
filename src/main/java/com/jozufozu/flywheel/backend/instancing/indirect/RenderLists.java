package com.jozufozu.flywheel.backend.instancing.indirect;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.model.Mesh;
import com.jozufozu.flywheel.util.Pair;

public class RenderLists {

	public final Map<Pair<StructType<?>, VertexType>, IndirectList<?>> lists = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <D extends InstancedPart> void add(IndirectInstancer<D> instancer, Material material, Mesh mesh) {
		var indirectList = (IndirectList<D>) lists.computeIfAbsent(Pair.of(instancer.structType, mesh.getVertexType()),
				p -> new IndirectList<>(p.first(), p.second()));

		indirectList.add(instancer, material, mesh);
	}
}
