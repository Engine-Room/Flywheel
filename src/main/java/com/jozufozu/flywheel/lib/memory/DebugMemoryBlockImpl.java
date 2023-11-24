package com.jozufozu.flywheel.lib.memory;

import java.lang.ref.Cleaner;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.lib.util.StringUtil;

class DebugMemoryBlockImpl extends MemoryBlockImpl {
	final CleaningAction cleaningAction;
	final Cleaner.Cleanable cleanable;

	DebugMemoryBlockImpl(long ptr, long size, Cleaner cleaner) {
		super(ptr, size);
		cleaningAction = new CleaningAction(ptr, size, getStackTrace());
		cleanable = cleaner.register(this, cleaningAction);
	}

	@Override
	void freeInner() {
		super.freeInner();
		cleaningAction.freed = true;
		cleanable.clean();
	}

	static StackWalker.StackFrame[] getStackTrace() {
		// skip 4 frames to get to the caller:
		// - this method
		// - DebugMemoryBlockImpl::new
		// - DebugMemoryBlockImpl::malloc/calloc
		// - MemoryBlock::malloc/calloc
		// - {caller is here}
		return StackWalker.getInstance().walk(s -> s.skip(4).toArray(StackWalker.StackFrame[]::new));
	}

	static MemoryBlock malloc(long size) {
		MemoryBlock block = new DebugMemoryBlockImpl(FlwMemoryTracker.malloc(size), size, FlwMemoryTracker.CLEANER);
		FlwMemoryTracker._allocCPUMemory(block.size());
		return block;
	}

	static MemoryBlock calloc(long num, long size) {
		MemoryBlock block = new DebugMemoryBlockImpl(FlwMemoryTracker.calloc(num, size), num * size, FlwMemoryTracker.CLEANER);
		FlwMemoryTracker._allocCPUMemory(block.size());
		return block;
	}

	static class CleaningAction implements Runnable {
		final long ptr;
		final long size;
		final StackWalker.StackFrame[] allocationSite;

		boolean freed;

		CleaningAction(long ptr, long size, StackWalker.StackFrame[] allocationSite) {
			this.ptr = ptr;
			this.size = size;
			this.allocationSite = allocationSite;
		}

		@Override
		public void run() {
			if (!freed) {
				StringBuilder builder = new StringBuilder();
				builder
					.append("Reclaimed ")
					.append(size)
					.append(" bytes at address ")
					.append(StringUtil.formatAddress(ptr))
					.append(" that were leaked from allocation site:");
				for (StackWalker.StackFrame frame : allocationSite) {
					builder.append("\n\t");
					builder.append(frame);
				}
				Flywheel.LOGGER.warn(builder.toString());

				FlwMemoryTracker.free(ptr);
				FlwMemoryTracker._freeCPUMemory(size);
			}
		}
	}
}
