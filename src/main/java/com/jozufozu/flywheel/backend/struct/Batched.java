package com.jozufozu.flywheel.backend.struct;

import com.jozufozu.flywheel.core.model.IModel;

public interface Batched<S> extends StructType<S> {

	BatchingTransformer<S> getTransformer(IModel model);

	@Override
	default Batched<S> asBatched() {
		return this;
	}
}
