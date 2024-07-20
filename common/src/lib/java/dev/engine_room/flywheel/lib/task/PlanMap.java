package dev.engine_room.flywheel.lib.task;

import java.util.ArrayList;
import java.util.List;

import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.task.TaskExecutor;

/**
 * A plan that executes a dynamic list of plans in parallel.
 *
 * <p>The plans can be added/removed by association with a key object.</p>
 *
 * @param <K> The key type
 * @param <C> The context type
 */
public final class PlanMap<K, C> implements SimplyComposedPlan<C> {
	private final List<K> keys = new ArrayList<>();
	private final List<Plan<C>> values = new ArrayList<>();

	public void add(K object, Plan<C> plan) {
		keys.add(object);
		values.add(plan);
	}

	public void remove(K object) {
		int index = keys.indexOf(object);

		if (index != -1) {
			keys.remove(index);
			values.remove(index);
		}
	}

	public void clear() {
		keys.clear();
		values.clear();
	}

	@Override
	public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
        Distribute.plans(taskExecutor, context, onCompletion, values);
	}
}
