package dev.engine_room.flywheel.backend.engine;

import dev.engine_room.flywheel.lib.memory.MemoryBlock;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class Arena {
	private final long elementSizeBytes;

	private MemoryBlock memoryBlock;

	// Monotonic index, generally represents the size of the arena.
	private int top = 0;
	// List of free indices.
	private final IntList freeStack = new IntArrayList();

	public Arena(long elementSizeBytes, int initialCapacity) {
		this.elementSizeBytes = elementSizeBytes;

		memoryBlock = MemoryBlock.malloc(elementSizeBytes * initialCapacity);
	}

	public int alloc() {
		// First re-use freed elements.
		if (!freeStack.isEmpty()) {
			return freeStack.removeInt(freeStack.size() - 1);
		}

		// Make sure there's room to increment top.
		if (top * elementSizeBytes >= memoryBlock.size()) {
			memoryBlock = memoryBlock.realloc(memoryBlock.size() * 2);
		}

		// Return the top index and increment.
		return top++;
	}

	public void free(int i) {
		// That's it! Now pls don't try to use it.
		freeStack.add(i);
	}

	public long indexToPointer(int i) {
		return memoryBlock.ptr() + i * elementSizeBytes;
	}

	public void delete() {
		memoryBlock.free();
	}

	public int capacity() {
		return top;
	}

	public long byteCapacity() {
		return memoryBlock.size();
	}
}
