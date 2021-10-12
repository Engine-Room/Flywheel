package com.jozufozu.flywheel.util;

import java.util.function.Consumer;

import javax.annotation.Nonnull;

public class Lazy<T> {

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

	/**
	 * Provides an external facing API safe way of invalidating lazy values.
	 */
	public static <T> Pair<Lazy<T>, KillSwitch<T>> ofKillable(NonNullSupplier<T> factory, Consumer<T> destructor) {
		Lazy<T> lazy = new Lazy<>(factory);

		KillSwitch<T> killSwitch = new KillSwitch<>(lazy, destructor);

		return Pair.of(lazy, killSwitch);
	}

	public static <T> Lazy<T> of(NonNullSupplier<T> factory) {
		return new Lazy<>(factory);
	}

	public static class KillSwitch<T> {

		private final Lazy<T> lazy;
		private final Consumer<T> finalizer;

		private KillSwitch(Lazy<T> lazy, Consumer<T> finalizer) {
			this.lazy = lazy;
			this.finalizer = finalizer;
		}

		public void killValue() {
			if (lazy.value != null) {
				finalizer.accept(lazy.value);
				lazy.value = null;
			}
		}
	}
}
