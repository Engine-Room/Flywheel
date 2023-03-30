package com.jozufozu.flywheel.backend.engine.indirect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.instancer.Instancer;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.instancing.InstancerKey;
import com.jozufozu.flywheel.util.Pair;

public class IndirectDrawManager {

	private final Map<InstancerKey<?>, IndirectInstancer<?>> instancers = new HashMap<>();
	private final List<UninitializedInstancer> uninitializedInstancers = new ArrayList<>();
	private final List<IndirectInstancer<?>> initializedInstancers = new ArrayList<>();
	public final Map<Pair<StructType<?>, VertexType>, IndirectCullingGroup<?>> renderLists = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <D extends InstancedPart> Instancer<D> getInstancer(StructType<D> type, Model model, RenderStage stage) {
		InstancerKey<D> key = new InstancerKey<>(type, model, stage);
		IndirectInstancer<D> instancer = (IndirectInstancer<D>) instancers.get(key);
		if (instancer == null) {
			instancer = new IndirectInstancer<>(type);
			instancers.put(key, instancer);
			uninitializedInstancers.add(new UninitializedInstancer(instancer, model, stage));
		}
		return instancer;
	}

	public void flush() {
		for (var instancer : uninitializedInstancers) {
			add(instancer.instancer(), instancer.model(), instancer.stage());
		}
		uninitializedInstancers.clear();

		for (IndirectCullingGroup<?> value : renderLists.values()) {
			value.beginFrame();
		}
	}

	public void delete() {
		instancers.clear();

		renderLists.values()
				.forEach(IndirectCullingGroup::delete);
		renderLists.clear();

		initializedInstancers.forEach(IndirectInstancer::delete);
		initializedInstancers.clear();
	}

	public void clearInstancers() {
		initializedInstancers.forEach(IndirectInstancer::clear);
	}

	@SuppressWarnings("unchecked")
	private <D extends InstancedPart> void add(IndirectInstancer<D> instancer, Model model, RenderStage stage) {
		var meshes = model.getMeshes();
		for (var entry : meshes.entrySet()) {
			var material = entry.getKey();
			var mesh = entry.getValue();

			var indirectList = (IndirectCullingGroup<D>) renderLists.computeIfAbsent(Pair.of(instancer.type, mesh.getVertexType()), p -> new IndirectCullingGroup<>(p.first(), p.second()));

			indirectList.drawSet.add(instancer, material, stage, indirectList.meshPool.alloc(mesh));

			break; // TODO: support multiple meshes per model
		}
		initializedInstancers.add(instancer);
	}

	private record UninitializedInstancer(IndirectInstancer<?> instancer, Model model, RenderStage stage) {
	}
}
