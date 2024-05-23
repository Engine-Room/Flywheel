package dev.engine_room.flywheel.lib.task.functional;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * A boolean supplier like interface for use with {@link dev.engine_room.flywheel.api.task.Plan Plans} and their contexts.
 *
 * @param <C> The context type.
 */
@FunctionalInterface
public interface BooleanSupplierWithContext<C> extends Predicate<C> {
	boolean getAsBoolean(C context);

	@Override
	default boolean test(C c) {
		return getAsBoolean(c);
	}

	/**
	 * A {@link BooleanSupplierWithContext} that ignores the context object.
	 *
	 * @param <C> The (ignored) context type.
	 */
	@FunctionalInterface
	interface Ignored<C> extends BooleanSupplierWithContext<C>, BooleanSupplier {
		@Override
		boolean getAsBoolean();

		@Override
		default boolean getAsBoolean(C ignored) {
			return getAsBoolean();
		}
	}
}
