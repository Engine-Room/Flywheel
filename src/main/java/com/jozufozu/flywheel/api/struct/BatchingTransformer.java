package com.jozufozu.flywheel.api.struct;

import com.jozufozu.flywheel.core.model.SuperByteBuffer;

@FunctionalInterface
public interface BatchingTransformer<S> {

	void transform(S s, SuperByteBuffer.Params b);
}
