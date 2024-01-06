package com.jozufozu.flywheel.backend.glsl.generate;

public enum BinOp {
	BITWISE_AND("&"),
	RIGHT_SHIFT(">>"),
	DIVIDE("/"),
	// TODO: add more as we need them
	;

	public final String op;

	BinOp(String op) {
		this.op = op;
	}
}
