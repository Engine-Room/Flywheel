package dev.engine_room.flywheel.backend.glsl.generate;

public enum BinOp {
	DIVIDE("/"),
	SUBTRACT("-"),
	RIGHT_SHIFT(">>"),
	BITWISE_AND("&"),
	BITWISE_XOR("^"),
	// TODO: add more as we need them
	;

	public final String op;

	BinOp(String op) {
		this.op = op;
	}
}
