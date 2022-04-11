package com.jozufozu.flywheel.api;

import javax.annotation.Nonnull;

import com.jozufozu.flywheel.core.model.Model;

public interface ModelSupplier {

	@Nonnull
	Model get();
}
