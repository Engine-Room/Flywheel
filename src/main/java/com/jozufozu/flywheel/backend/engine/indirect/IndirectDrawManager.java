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
	private final StagingBuffer stagingBuffer = new StagingBuffer();
	private final Map<InstanceType<?>, IndirectCullingGroup<?>> cullingGroups = new HashMap<>();

	@Override
	protected <I extends Instance> IndirectInstancer<?> create(InstanceType<I> type) {
		return new IndirectInstancer<>(type);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <I extends Instance> void add(InstancerKey<I> key, IndirectInstancer<?> instancer, Model model, RenderStage stage) {
		var group = (IndirectCullingGroup<I>) cullingGroups.computeIfAbsent(key.type(), IndirectCullingGroup::new);
		group.add((IndirectInstancer<I>) instancer, model, stage);
	}

	public boolean hasStage(RenderStage stage) {
		for (var group : cullingGroups.values()) {
			if (group.hasStage(stage)) {
				return true;
			}
		}
		return false;
	}

	public void renderStage(RenderStage stage) {
		for (var group : cullingGroups.values()) {
			group.submit(stage);
		}
	}

	@Override
	public void flush() {
		super.flush();

		stagingBuffer.reclaim();

		for (var group : cullingGroups.values()) {
			group.flush(stagingBuffer);
		}

		stagingBuffer.flush();

		for (var group : cullingGroups.values()) {
			group.dispatchCull();
		}

		for (var group : cullingGroups.values()) {
			group.dispatchApply();
		}
	}

	@Override
	public void delete() {
		super.delete();

		cullingGroups.values()
				.forEach(IndirectCullingGroup::delete);
		cullingGroups.clear();

		stagingBuffer.delete();
	}
}
