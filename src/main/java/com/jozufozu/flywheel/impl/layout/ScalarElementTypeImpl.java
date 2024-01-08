package com.jozufozu.flywheel.impl.layout;

import com.jozufozu.flywheel.api.layout.ScalarElementType;
import com.jozufozu.flywheel.api.layout.ValueRepr;
import com.jozufozu.flywheel.lib.math.MoreMath;

record ScalarElementTypeImpl(ValueRepr repr, int byteSize) implements ScalarElementType {
	static ScalarElementTypeImpl create(ValueRepr repr) {
		return new ScalarElementTypeImpl(repr, MoreMath.align4(repr.byteSize()));
	}
}
