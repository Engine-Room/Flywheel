package com.jozufozu.flywheel.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * A supplier whose value should never be null.
 * @param <T>
 */
@FunctionalInterface
public interface NonNullSupplier<T> extends Supplier<T> {
	@NotNull
	T get();
}
