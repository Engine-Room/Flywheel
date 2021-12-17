package com.jozufozu.flywheel.api.struct;

import com.jozufozu.flywheel.core.model.ModelTransformer;

@FunctionalInterface
public interface BatchingTransformer<S> {

	void transform(S s, ModelTransformer.Params b);
}
