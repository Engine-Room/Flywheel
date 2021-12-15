package com.jozufozu.flywheel.api.struct;

public interface Batched<S> extends StructType<S> {

	BatchingTransformer<S> getTransformer();

	@Override
	default Batched<S> asBatched() {
		return this;
	}
}
