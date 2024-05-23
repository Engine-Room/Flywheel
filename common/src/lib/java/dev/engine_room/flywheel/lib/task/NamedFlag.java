package dev.engine_room.flywheel.lib.task;

/**
 * A flag with an arbitrary name.
 */
public final class NamedFlag extends Flag {
	private final String name;

	/**
	 * @param name The name of the flag, mainly for debugging purposes.
	 */
	public NamedFlag(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "NamedFlag[" + "name=" + name + ']';
	}

}
