package dev.engine_room.flywheel.backend.engine;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public abstract class AbstractArena {
	protected final long elementSizeBytes;
	// List of free indices.
	private final IntList freeStack = new IntArrayList();
	// Monotonic index, generally represents the size of the arena.
	private int top = 0;

	public AbstractArena(long elementSizeBytes) {
		this.elementSizeBytes = elementSizeBytes;
	}

	public int alloc() {
		// First re-use freed elements.
		if (!freeStack.isEmpty()) {
			return freeStack.removeInt(freeStack.size() - 1);
		}

		// Make sure there's room to increment top.
		if (top * elementSizeBytes >= byteCapacity()) {
			resize();
		}

		// Return the top index and increment.
		return top++;
	}

	public void free(int i) {
		// That's it! Now pls don't try to use it.
		freeStack.add(i);
	}

	public long byteOffsetOf(int i) {
		return i * elementSizeBytes;
	}

	public int capacity() {
		return top;
	}

	public abstract long byteCapacity();

	protected abstract void resize();
}
