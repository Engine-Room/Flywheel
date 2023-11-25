package com.jozufozu.flywheel.backend.engine.indirect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.engine.InstancerKey;
import com.jozufozu.flywheel.lib.util.Pair;

public class IndirectDrawManager {
	private final Map<InstancerKey<?>, IndirectInstancer<?>> instancers = new HashMap<>();
	private final List<UninitializedInstancer> uninitializedInstancers = new ArrayList<>();
	private final List<IndirectInstancer<?>> initializedInstancers = new ArrayList<>();
	public final Map<Pair<InstanceType<?>, VertexType>, IndirectCullingGroup<?>> renderLists = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <I extends Instance> Instancer<I> getInstancer(InstanceType<I> type, Model model, RenderStage stage) {
		InstancerKey<I> key = new InstancerKey<>(type, model, stage);
		// FIXME: This needs to be synchronized like InstancingEngine
		IndirectInstancer<I> instancer = (IndirectInstancer<I>) instancers.get(key);
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

	public void invalidate() {
		instancers.clear();

		renderLists.values()
				.forEach(IndirectCullingGroup::delete);
		renderLists.clear();

		initializedInstancers.clear();
	}

	public void clearInstancers() {
		initializedInstancers.forEach(IndirectInstancer::clear);
	}

	@SuppressWarnings("unchecked")
	private <I extends Instance> void add(IndirectInstancer<I> instancer, Model model, RenderStage stage) {
		var meshes = model.getMeshes();
		for (var entry : meshes.entrySet()) {
			var material = entry.getKey();
			var mesh = entry.getValue();

			var indirectList = (IndirectCullingGroup<I>) renderLists.computeIfAbsent(Pair.of(instancer.type, mesh.vertexType()), p -> new IndirectCullingGroup<>(p.first(), p.second()));

			indirectList.add(instancer, stage, material, mesh);

			break; // TODO: support multiple meshes per model
		}
		initializedInstancers.add(instancer);
	}

	public boolean hasStage(RenderStage stage) {
		for (var list : renderLists.values()) {
			if (list.hasStage(stage)) {
				return true;
			}
		}
		return false;
	}

	private record UninitializedInstancer(IndirectInstancer<?> instancer, Model model, RenderStage stage) {
	}
}
