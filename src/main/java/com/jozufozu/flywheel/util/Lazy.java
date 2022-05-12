package com.jozufozu.flywheel.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

public class Lazy<T> implements Supplier<T> {

	private final NonNullSupplier<T> supplier;

	private T value;

	public Lazy(NonNullSupplier<T> supplier) {
		this.supplier = supplier;
	}

	@NotNull
	public T get() {
		if (value == null) {
			value = supplier.get();
		}

		return value;
	}

	public <Q> Lazy<Q> lazyMap(Function<T, Q> func) {
		return new Lazy<>(() -> func.apply(get()));
	}

	public static <T> Lazy<T> of(NonNullSupplier<T> factory) {
		return new Lazy<>(factory);
	}

	public void ifPresent(Consumer<T> func) {
		if (value != null) {
			func.accept(value);
		}
	}

	/**
	 * If initialized, maps the stored value based on the function, otherwise returns the None.
	 * @param func The function to map the value with.
	 * @param <Q> The type of the mapped value.
	 * @return The mapped value, or None if not initialized.
	 */
	public <Q> Optional<Q> map(Function<T, Q> func) {
		if (value != null) {
			return Optional.of(func.apply(value));
		} else {
			return Optional.empty();
		}
	}
}
