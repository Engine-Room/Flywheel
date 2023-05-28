package com.jozufozu.flywheel.lib.task;

import java.util.function.Function;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;

public class UnitPlan<C> implements Plan<C> {
	private static final UnitPlan<?> INSTANCE = new UnitPlan<>();

	private UnitPlan() {
	}

	@SuppressWarnings("unchecked")
	public static <C> UnitPlan<C> of() {
		return (UnitPlan<C>) INSTANCE;
	}

	@Override
	public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
		onCompletion.run();
	}

	@Override
	public Plan<C> then(Plan<C> plan) {
		return plan;
	}

	@Override
	public <D> Plan<C> thenMap(Function<C, D> map, Plan<D> plan) {
		return new MapContextPlan<>(map, plan);
	}

	@Override
	public Plan<C> and(Plan<C> plan) {
		return plan;
	}

	@Override
	public <D> Plan<C> andMap(Function<C, D> map, Plan<D> plan) {
		return new MapContextPlan<>(map, plan);
	}

	@Override
	public Plan<C> simplify() {
		return this;
	}
}
