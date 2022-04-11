package com.jozufozu.flywheel.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class Lazy<T> implements Supplier<T> {

	private final NonNullSupplier<T> supplier;

	private T value;

	public Lazy(NonNullSupplier<T> supplier) {
		this.supplier = supplier;
	}

	@Nonnull
	public T get() {
		if (value == null) {
			value = supplier.get();
		}

		return value;
	}

	public static <T> Lazy<T> of(NonNullSupplier<T> factory) {
		return new Lazy<>(factory);
	}

	public void ifPresent(Consumer<T> func) {
		if (value != null) {
			func.accept(value);
		}
	}
}
