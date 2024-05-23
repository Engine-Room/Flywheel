package dev.engine_room.flywheel.api.model;

/**
 * Represents a sequence of unsigned integer vertex indices.
 */
public interface IndexSequence {
	/**
	 * Populate the given memory region with indices.
	 * <p>
	 * Do not write outside the range {@code [ptr, ptr + count * 4]}.
	 */
	void fill(long ptr, int count);
}
