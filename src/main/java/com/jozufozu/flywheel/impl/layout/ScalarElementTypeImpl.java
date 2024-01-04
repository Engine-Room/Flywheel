package com.jozufozu.flywheel.impl.layout;

import com.jozufozu.flywheel.api.layout.ScalarElementType;
import com.jozufozu.flywheel.api.layout.ValueRepr;

final class ScalarElementTypeImpl implements ScalarElementType {
	private final ValueRepr repr;
	private final int byteSize;

	ScalarElementTypeImpl(ValueRepr repr) {
		this.repr = repr;
		byteSize = repr.byteSize();
	}

	@Override
	public ValueRepr repr() {
		return repr;
	}

	@Override
	public int byteSize() {
		return byteSize;
	}
}
