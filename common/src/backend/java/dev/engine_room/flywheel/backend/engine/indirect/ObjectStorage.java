package dev.engine_room.flywheel.backend.engine.indirect;

import java.util.Arrays;
import java.util.BitSet;

import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.backend.engine.AbstractArena;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;

public class ObjectStorage extends AbstractArena {
	// 32 objects per page. Allows for convenient bitsets on the gpu.
	public static final int LOG_2_PAGE_SIZE = 5;
	public static final int PAGE_SIZE = 1 << LOG_2_PAGE_SIZE;
	public static final int PAGE_MASK = PAGE_SIZE - 1;

	public static final int INVALID_PAGE = -1;

	public static final int INITIAL_PAGES_ALLOCATED = 4;
	public static final int DESCRIPTOR_SIZE_BYTES = Integer.BYTES * 2;

	private final BitSet changedFrames = new BitSet();
	/**
	 * The GPU side buffer containing all the objects, logically divided into page frames.
	 */
	public final ResizableStorageBuffer objectBuffer;
	/**
	 * The GPU side buffer containing 32 bit descriptors for each page frame.
	 */
	public final ResizableStorageBuffer frameDescriptorBuffer;
	/**
	 * The CPU side memory block containing the page descriptors.
	 */
	private MemoryBlock frameDescriptors;

	public ObjectStorage(long objectSizeBytes) {
		super(PAGE_SIZE * objectSizeBytes);

		this.objectBuffer = new ResizableStorageBuffer();
		this.frameDescriptorBuffer = new ResizableStorageBuffer();

		objectBuffer.ensureCapacity(INITIAL_PAGES_ALLOCATED * elementSizeBytes);
		frameDescriptorBuffer.ensureCapacity(INITIAL_PAGES_ALLOCATED * DESCRIPTOR_SIZE_BYTES);
		frameDescriptors = MemoryBlock.malloc(INITIAL_PAGES_ALLOCATED * DESCRIPTOR_SIZE_BYTES);
	}

	public Mapping createMapping() {
		return new Mapping();
	}

	@Override
	public long byteCapacity() {
		return objectBuffer.capacity();
	}

	@Override
	public void free(int i) {
		if (i == INVALID_PAGE) {
			return;
		}
		super.free(i);
		var ptr = ptrForPage(i);
		MemoryUtil.memPutInt(ptr, 0);
		MemoryUtil.memPutInt(ptr + 4, 0);

		changedFrames.set(i);
	}

	private void set(int i, int modelIndex, int validBits) {
		var ptr = ptrForPage(i);
		MemoryUtil.memPutInt(ptr, modelIndex);
		MemoryUtil.memPutInt(ptr + 4, validBits);

		changedFrames.set(i);
	}

	@Override
	protected void grow() {
		objectBuffer.ensureCapacity(objectBuffer.capacity() * 2);
		frameDescriptorBuffer.ensureCapacity(frameDescriptorBuffer.capacity() * 2);
		frameDescriptors = frameDescriptors.realloc(frameDescriptors.size() * 2);
	}

	public void uploadDescriptors(StagingBuffer stagingBuffer) {
		if (changedFrames.isEmpty()) {
			return;
		}

		var ptr = frameDescriptors.ptr();
		for (int i = changedFrames.nextSetBit(0); i >= 0 && i < capacity(); i = changedFrames.nextSetBit(i + 1)) {
			var offset = (long) i * DESCRIPTOR_SIZE_BYTES;
			stagingBuffer.enqueueCopy(ptr + offset, DESCRIPTOR_SIZE_BYTES, frameDescriptorBuffer.handle(), offset);
		}

		changedFrames.clear();
	}

	public void delete() {
		objectBuffer.delete();
		frameDescriptorBuffer.delete();
		frameDescriptors.free();
	}

	private long ptrForPage(int page) {
		return frameDescriptors.ptr() + (long) page * DESCRIPTOR_SIZE_BYTES;
	}

	public static int objectIndex2PageIndex(int objectIndex) {
		return objectIndex >> LOG_2_PAGE_SIZE;
	}

	public static int pageIndex2ObjectIndex(int pageIndex) {
		return pageIndex << LOG_2_PAGE_SIZE;
	}

	/**
	 * Maps serial object indices to pages, and manages the allocation of pages.
	 */
	public class Mapping {
		private static final int[] EMPTY_ALLOCATION = new int[0];
		private int[] pages = EMPTY_ALLOCATION;

		public void updatePage(int index, int modelIndex, int validBits) {
			if (validBits == 0) {
				holePunch(index);
				return;
			}
			var frame = pages[index];

			if (frame == INVALID_PAGE) {
				// Un-holed punch.
				frame = unHolePunch(index);
			}

			ObjectStorage.this.set(frame, modelIndex, validBits);
		}

		/**
		 * Free a page on the inside of the mapping, maintaining the same virtual mapping size.
		 *
		 * @param index The index of the page to free.
		 */
		public void holePunch(int index) {
			ObjectStorage.this.free(pages[index]);
			pages[index] = INVALID_PAGE;
		}

		/**
		 * Allocate a new page on the inside of the mapping, maintaining the same virtual mapping size.
		 *
		 * @param index The index of the page to allocate.
		 * @return The allocated page.
		 */
		private int unHolePunch(int index) {
			int page = ObjectStorage.this.alloc();
			pages[index] = page;
			return page;
		}

		public void updateCount(int newLength) {
			var oldLength = pages.length;
			if (oldLength > newLength) {
				// Eagerly free the now unnecessary pages.
				// shrink will zero out the pageTable entries for the freed pages.
				shrink(oldLength, newLength);
			} else if (oldLength < newLength) {
				// Allocate new pages to fit the new object count.
				grow(newLength, oldLength);
			}
		}

		public int pageCount() {
			return pages.length;
		}

		public long page2ByteOffset(int index) {
			return ObjectStorage.this.byteOffsetOf(pages[index]);
		}

		public void delete() {
			for (int page : pages) {
				ObjectStorage.this.free(page);
			}
			pages = EMPTY_ALLOCATION;
		}

		private void grow(int neededPages, int oldLength) {
			pages = Arrays.copyOf(pages, neededPages);

			for (int i = oldLength; i < neededPages; i++) {
				var page = ObjectStorage.this.alloc();
				pages[i] = page;
			}
		}

		private void shrink(int oldLength, int neededPages) {
			for (int i = oldLength - 1; i >= neededPages; i--) {
				var page = pages[i];
				ObjectStorage.this.free(page);
			}

			pages = Arrays.copyOf(pages, neededPages);
		}

		public int objectIndex2GlobalIndex(int objectIndex) {
			return (pages[objectIndex2PageIndex(objectIndex)] << LOG_2_PAGE_SIZE) + (objectIndex & PAGE_MASK);
		}
	}
}
