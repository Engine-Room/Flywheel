package com.jozufozu.flywheel.core;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.jozufozu.flywheel.api.ModelSupplier;
import com.jozufozu.flywheel.core.model.Model;
import com.jozufozu.flywheel.util.Lazy;
import com.jozufozu.flywheel.util.NonNullSupplier;

public class SimpleModelSupplier implements ModelSupplier {

	private final Lazy<Model> supplier;

	public SimpleModelSupplier(NonNullSupplier<Model> supplier) {
		this.supplier = Lazy.of(supplier);
	}

	@Nonnull
	@Override
	public Model get() {
		return supplier.get();
	}
}
