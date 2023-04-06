package com.jozufozu.flywheel.backend.engine.indirect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instancer.InstancePart;
import com.jozufozu.flywheel.api.instancer.Instancer;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.engine.InstancerKey;
import com.jozufozu.flywheel.util.Pair;

public class IndirectDrawManager {

	private final Map<InstancerKey<?>, IndirectInstancer<?>> instancers = new HashMap<>();
	private final List<UninitializedInstancer> uninitializedInstancers = new ArrayList<>();
	private final List<IndirectInstancer<?>> initializedInstancers = new ArrayList<>();
	public final Map<Pair<StructType<?>, VertexType>, IndirectCullingGroup<?>> renderLists = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <P extends InstancePart> Instancer<P> getInstancer(StructType<P> type, Model model, RenderStage stage) {
		InstancerKey<P> key = new InstancerKey<>(type, model, stage);
		IndirectInstancer<P> instancer = (IndirectInstancer<P>) instancers.get(key);
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

		initializedInstancers.clear();
	}

	public void clearInstancers() {
		initializedInstancers.forEach(IndirectInstancer::clear);
	}

	@SuppressWarnings("unchecked")
	private <P extends InstancePart> void add(IndirectInstancer<P> instancer, Model model, RenderStage stage) {
		var meshes = model.getMeshes();
		for (var entry : meshes.entrySet()) {
			var material = entry.getKey();
			var mesh = entry.getValue();

			var indirectList = (IndirectCullingGroup<P>) renderLists.computeIfAbsent(Pair.of(instancer.type, mesh.getVertexType()), p -> new IndirectCullingGroup<>(p.first(), p.second()));

			indirectList.drawSet.add(instancer, material, stage, indirectList.meshPool.alloc(mesh));

			break; // TODO: support multiple meshes per model
		}
		initializedInstancers.add(instancer);
	}

	private record UninitializedInstancer(IndirectInstancer<?> instancer, Model model, RenderStage stage) {
	}
}
