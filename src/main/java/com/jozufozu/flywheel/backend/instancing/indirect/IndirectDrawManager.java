package com.jozufozu.flywheel.backend.instancing.indirect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.model.Model;
import com.jozufozu.flywheel.util.Pair;

public class IndirectDrawManager {

	private final List<UninitializedModel> uninitializedModels = new ArrayList<>();
	private final List<IndirectInstancer<?>> allInstancers = new ArrayList<>();
	public final Map<Pair<StructType<?>, VertexType>, IndirectCullingGroup<?>> renderLists = new HashMap<>();

	public void create(IndirectInstancer<?> instancer, Model model) {
		uninitializedModels.add(new UninitializedModel(instancer, model));
	}

	public void flush() {
		for (var model : uninitializedModels) {
			add(model.instancer(), model.model());
		}
		uninitializedModels.clear();

		for (IndirectCullingGroup<?> value : renderLists.values()) {
			value.beginFrame();
		}
	}

	public void delete() {
		renderLists.values()
				.forEach(IndirectCullingGroup::delete);
		renderLists.clear();

		allInstancers.forEach(IndirectInstancer::delete);
		allInstancers.clear();
	}

	public void clearInstancers() {
		allInstancers.forEach(IndirectInstancer::clear);
	}

	@SuppressWarnings("unchecked")
	private <D extends InstancedPart> void add(IndirectInstancer<D> instancer, Model model) {
		var meshes = model.getMeshes();
		for (var entry : meshes.entrySet()) {
			var material = entry.getKey();
			var mesh = entry.getValue();

			var indirectList = (IndirectCullingGroup<D>) renderLists.computeIfAbsent(Pair.of(instancer.type, mesh.getVertexType()), p -> new IndirectCullingGroup<>(p.first(), p.second()));

			indirectList.drawSet.add(instancer, material, indirectList.meshPool.alloc(mesh));

			break; // TODO: support multiple meshes per model
		}
		allInstancers.add(instancer);
	}

	private record UninitializedModel(IndirectInstancer<?> instancer, Model model) {
	}
}
