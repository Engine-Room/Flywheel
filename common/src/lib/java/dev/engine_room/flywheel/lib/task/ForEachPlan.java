package dev.engine_room.flywheel.lib.task;

import java.util.List;

import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.task.TaskExecutor;
import dev.engine_room.flywheel.lib.task.functional.ConsumerWithContext;
import dev.engine_room.flywheel.lib.task.functional.SupplierWithContext;

/**
 * A plan that executes code on each element of a provided list.
 * <p>
 * Operations are dynamically batched based on the number of available threads.
 *
 * @param listSupplier A supplier of the list to iterate over.
 * @param action       The action to perform on each element.
 * @param <T>          The type of the list elements.
 * @param <C>          The type of the context object.
 */
public record ForEachPlan<T, C>(SupplierWithContext<C, List<T>> listSupplier,
								ConsumerWithContext<T, C> action) implements SimplyComposedPlan<C> {
	public static <T, C> Plan<C> of(SupplierWithContext<C, List<T>> iterable, ConsumerWithContext<T, C> forEach) {
		return new ForEachPlan<>(iterable, forEach);
	}

	public static <T, C> Plan<C> of(SupplierWithContext<C, List<T>> iterable, ConsumerWithContext.Ignored<T, C> forEach) {
		return new ForEachPlan<>(iterable, forEach);
	}

	public static <T, C> Plan<C> of(SupplierWithContext.Ignored<C, List<T>> iterable, ConsumerWithContext<T, C> forEach) {
		return new ForEachPlan<>(iterable, forEach);
	}

	public static <T, C> Plan<C> of(SupplierWithContext.Ignored<C, List<T>> iterable, ConsumerWithContext.Ignored<T, C> forEach) {
		return new ForEachPlan<>(iterable, forEach);
	}

	@Override
	public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
		taskExecutor.execute(() -> Distribute.tasks(taskExecutor, context, onCompletion, listSupplier.get(context), action));
	}
}
