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
	private final Map<InstanceType<?>, IndirectCullingGroup<?>> renderLists = new HashMap<>();

	@Override
	protected <I extends Instance> IndirectInstancer<?> create(InstanceType<I> type) {
		return new IndirectInstancer<>(type);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <I extends Instance> void add(InstancerKey<I> key, IndirectInstancer<?> instancer, Model model, RenderStage stage) {
		var group = (IndirectCullingGroup<I>) renderLists.computeIfAbsent(key.type(), IndirectCullingGroup::new);
		group.add((IndirectInstancer<I>) instancer, model, stage);
	}

	public boolean hasStage(RenderStage stage) {
		for (var group : renderLists.values()) {
			if (group.hasStage(stage)) {
				return true;
			}
		}
		return false;
	}

	public void renderStage(RenderStage stage) {
		for (var group : renderLists.values()) {
			group.submit(stage);
		}
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
