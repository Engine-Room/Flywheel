package dev.engine_room.flywheel.lib.memory;

class MemoryBlockImpl extends AbstractMemoryBlockImpl {
	MemoryBlockImpl(long ptr, long size) {
		super(ptr, size);
	}

	@Override
	public boolean isTracked() {
		return false;
	}

	@Override
	public MemoryBlock realloc(long size) {
		MemoryBlock block = new MemoryBlockImpl(FlwMemoryTracker.realloc(ptr, size), size);
		FlwMemoryTracker._allocCPUMemory(block.size());
		freeInner();
		return block;
	}

	static MemoryBlock malloc(long size) {
		MemoryBlock block = new MemoryBlockImpl(FlwMemoryTracker.malloc(size), size);
		FlwMemoryTracker._allocCPUMemory(block.size());
		return block;
	}

	static MemoryBlock calloc(long num, long size) {
		MemoryBlock block = new MemoryBlockImpl(FlwMemoryTracker.calloc(num, size), num * size);
		FlwMemoryTracker._allocCPUMemory(block.size());
		return block;
	}
}
