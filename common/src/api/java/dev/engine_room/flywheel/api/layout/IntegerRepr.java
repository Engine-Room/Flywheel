package dev.engine_room.flywheel.api.layout;

public enum IntegerRepr implements ValueRepr {
	BYTE(Byte.BYTES),
	SHORT(Short.BYTES),
	INT(Integer.BYTES);

	private final int byteSize;

	IntegerRepr(int byteSize) {
		this.byteSize = byteSize;
	}

	@Override
	public int byteSize() {
		return byteSize;
	}
}
