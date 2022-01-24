package com.jozufozu.flywheel.config;

public enum BooleanDirective {
	TRUE(true),
	FALSE(false),
	/**
	 * Don't change anything, just display what the value currently is.
	 */
	DISPLAY(true),
	;

	private final boolean b;

	BooleanDirective(boolean b) {
		this.b = b;
	}

	public boolean get() {
		if (this == DISPLAY) throw new IllegalStateException("DISPLAY directive has no value");
		return b;
	}
}
