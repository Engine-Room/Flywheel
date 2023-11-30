package com.jozufozu.flywheel.lib.task.functional;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A supplier like interface for use with {@link com.jozufozu.flywheel.api.task.Plan Plans} and their contexts.
 * <br>
 * The subinterface {@link Ignored} is provided for suppliers that do not need the context object.
 * @param <C> The context type.
 * @param <R> The return type.
 */
@FunctionalInterface
public interface SupplierWithContext<C, R> extends Function<C, R> {
	R get(C context);

	@Override
	default R apply(C c) {
		return get(c);
	}

	/**
	 * A {@link SupplierWithContext} that ignores the context object.
	 *
	 * @param <C> The (ignored) context type.
	 * @param <R> The return type.
	 */
	@FunctionalInterface
	interface Ignored<C, R> extends SupplierWithContext<C, R>, Supplier<R> {
		@Override
		R get();

		@Override
		default R get(C ignored) {
			return get();
		}
	}
}
