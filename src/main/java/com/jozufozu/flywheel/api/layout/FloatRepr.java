package com.jozufozu.flywheel.api.layout;

public enum FloatRepr implements ValueRepr {
	BYTE(Byte.BYTES),
	NORMALIZED_BYTE(Byte.BYTES),
	UNSIGNED_BYTE(Byte.BYTES),
	NORMALIZED_UNSIGNED_BYTE(Byte.BYTES),
	SHORT(Short.BYTES),
	NORMALIZED_SHORT(Short.BYTES),
	UNSIGNED_SHORT(Short.BYTES),
	NORMALIZED_UNSIGNED_SHORT(Short.BYTES),
	INT(Integer.BYTES),
	NORMALIZED_INT(Integer.BYTES),
	UNSIGNED_INT(Integer.BYTES),
	NORMALIZED_UNSIGNED_INT(Integer.BYTES),
	FLOAT(Float.BYTES);

	private final int byteSize;

	FloatRepr(int byteSize) {
		this.byteSize = byteSize;
	}

	@Override
	public int byteSize() {
		return byteSize;
	}
}
