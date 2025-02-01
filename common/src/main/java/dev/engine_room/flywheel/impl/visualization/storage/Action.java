package dev.engine_room.flywheel.impl.visualization.storage;

public enum Action {
	ADD(1),
	REMOVE(2),
	UPDATE(4),
	;

	private final int bit;

	Action(int bit) {
		this.bit = bit;
	}

	public int setBit(int i) {
		return i | bit;
	}

	public boolean in(int i) {
		return (i & bit) != 0;
	}
}
