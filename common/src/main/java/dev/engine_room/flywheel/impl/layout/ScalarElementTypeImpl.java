package dev.engine_room.flywheel.impl.layout;

import dev.engine_room.flywheel.api.layout.ScalarElementType;
import dev.engine_room.flywheel.api.layout.ValueRepr;

record ScalarElementTypeImpl(ValueRepr repr, int byteSize, int byteAlignment) implements ScalarElementType {
	static ScalarElementTypeImpl create(ValueRepr repr) {
		return new ScalarElementTypeImpl(repr, repr.byteSize(), repr.byteSize());
	}
}
