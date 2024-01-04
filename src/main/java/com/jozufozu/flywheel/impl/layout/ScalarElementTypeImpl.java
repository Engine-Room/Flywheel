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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + repr.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ScalarElementTypeImpl other = (ScalarElementTypeImpl) obj;
		return repr == other.repr;
	}
}
