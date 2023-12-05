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
	public final Map<InstanceType<?>, IndirectCullingGroup<?>> cullingGroups = new HashMap<>();

	@Override
	protected <I extends Instance> IndirectInstancer<?> create(InstanceType<I> type) {
		return new IndirectInstancer<>(type);
	}

	@Override
	protected <I extends Instance> void add(InstancerKey<I> key, IndirectInstancer<?> instancer, Model model, RenderStage stage) {
		var indirectList = (IndirectCullingGroup<I>) cullingGroups.computeIfAbsent(key.type(), IndirectCullingGroup::new);

		indirectList.add((IndirectInstancer<I>) instancer, stage, model);
	}

	public boolean hasStage(RenderStage stage) {
		for (var list : cullingGroups.values()) {
			if (list.hasStage(stage)) {
				return true;
			}
		}
		return false;
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
