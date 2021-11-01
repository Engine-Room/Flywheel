package com.jozufozu.flywheel.config;

import net.minecraft.network.PacketBuffer;

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

	/**
	 * Encode a variant of BooleanDirective. Symmetrical function to {@link #decode}
	 */
	public void encode(PacketBuffer buffer) {
		buffer.writeByte(this.ordinal());
	}

	/**
	 * Safely decode a variant of BooleanDirective. Symmetrical function to {@link #encode}
	 */
	public static BooleanDirective decode(PacketBuffer buffer) {
		return values()[buffer.readByte()];
	}
}
