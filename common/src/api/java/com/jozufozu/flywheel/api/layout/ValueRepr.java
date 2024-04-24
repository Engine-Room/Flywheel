package com.jozufozu.flywheel.api.layout;

public sealed interface ValueRepr permits IntegerRepr, UnsignedIntegerRepr, FloatRepr {
	int byteSize();
}
