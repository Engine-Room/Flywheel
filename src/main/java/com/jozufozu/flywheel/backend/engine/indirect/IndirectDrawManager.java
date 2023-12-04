package com.jozufozu.flywheel.backend.engine.indirect;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.backend.engine.InstancerKey;
import com.jozufozu.flywheel.backend.engine.InstancerStorage;

public class IndirectDrawManager extends InstancerStorage<IndirectInstancer<?>> {
	public final Map<InstanceType<?>, IndirectCullingGroup<?>> renderLists = new HashMap<>();

	@Override
	protected <I extends Instance> IndirectInstancer<?> create(InstanceType<I> type) {
		return new IndirectInstancer<>(type);
	}

	@Override
	protected <I extends Instance> void add(InstancerKey<I> key, IndirectInstancer<?> instancer, Model model, RenderStage stage) {
		var indirectList = (IndirectCullingGroup<I>) renderLists.computeIfAbsent(key.type(), IndirectCullingGroup::new);

		indirectList.add((IndirectInstancer<I>) instancer, stage, model);
	}

	public boolean hasStage(RenderStage stage) {
		for (var list : renderLists.values()) {
			if (list.hasStage(stage)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void flush() {
		super.flush();

		for (var group : renderLists.values()) {
			group.flush();
		}

		for (var group : renderLists.values()) {
			group.dispatchCull();
		}

		for (var group : renderLists.values()) {
			group.dispatchApply();
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();

		renderLists.values()
				.forEach(IndirectCullingGroup::delete);
		renderLists.clear();
	}
}
