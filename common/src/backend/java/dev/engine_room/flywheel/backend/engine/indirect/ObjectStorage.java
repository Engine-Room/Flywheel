package dev.engine_room.flywheel.backend.engine.indirect;

import java.util.Arrays;

import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.backend.engine.AbstractArena;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;

public class ObjectStorage extends AbstractArena {
	// 32 objects per page. Allows for convenient bitsets on the gpu.
	public static final int LOG_2_PAGE_SIZE = 5;
	public static final int PAGE_SIZE = 1 << LOG_2_PAGE_SIZE;
	public static final int PAGE_MASK = PAGE_SIZE - 1;

	public static final int INITIAL_PAGES_ALLOCATED = 4;

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

	private boolean needsUpload = false;

	public ObjectStorage(long objectSizeBytes) {
		super(PAGE_SIZE * objectSizeBytes);

		this.objectBuffer = new ResizableStorageBuffer();
		this.frameDescriptorBuffer = new ResizableStorageBuffer();

		objectBuffer.ensureCapacity(INITIAL_PAGES_ALLOCATED * elementSizeBytes);
		frameDescriptorBuffer.ensureCapacity(INITIAL_PAGES_ALLOCATED * Integer.BYTES);
		frameDescriptors = MemoryBlock.malloc(INITIAL_PAGES_ALLOCATED * Integer.BYTES);
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
		super.free(i);
		MemoryUtil.memPutInt(ptrForPage(i), 0);
	}

	@Override
	protected void grow() {
		objectBuffer.ensureCapacity(objectBuffer.capacity() * 2);
		frameDescriptorBuffer.ensureCapacity(frameDescriptorBuffer.capacity() * 2);
		frameDescriptors = frameDescriptors.realloc(frameDescriptors.size() * 2);
	}

	public void uploadDescriptors(StagingBuffer stagingBuffer) {
		if (!needsUpload) {
			return;
		}
		// We could be smarter about which spans are uploaded but this thing is so small it's probably not worth it.
		stagingBuffer.enqueueCopy(frameDescriptors.ptr(), frameDescriptors.size(), frameDescriptorBuffer.handle(), 0);
		needsUpload = false;
	}

	public void delete() {
		objectBuffer.delete();
		frameDescriptorBuffer.delete();
		frameDescriptors.free();
	}

	private long ptrForPage(int page) {
		return frameDescriptors.ptr() + (long) page * Integer.BYTES;
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

		private int modelIndex = -1;
		private int objectCount = 0;

		/**
		 * Adjust this allocation to the given model index and object count.
		 *
		 * <p>This method triggers eager resizing of the allocation to fit the new object count.
		 * If the model index is different from the current one, all frame descriptors will be updated.
		 *
		 * @param modelIndex The model index the objects in this allocation are associated with.
		 * @param objectCount The number of objects in this allocation.
		 */
		public void update(int modelIndex, int objectCount) {
			boolean incremental = this.modelIndex == modelIndex;

			if (incremental && objectCount == this.objectCount) {
				// Nothing will change.
				return;
			}

			ObjectStorage.this.needsUpload = true;

			this.modelIndex = modelIndex;
			this.objectCount = objectCount;

			var oldLength = pages.length;
			var newLength = objectIndex2PageIndex((objectCount + PAGE_MASK));

			if (oldLength > newLength) {
				// Eagerly free the now unnecessary pages.
				// shrink will zero out the pageTable entries for the freed pages.
				shrink(oldLength, newLength);

				if (incremental) {
					// Only update the last page, everything else is unchanged.
					updateRange(newLength - 1, newLength);
				}
			} else if (oldLength < newLength) {
				// Allocate new pages to fit the new object count.
				grow(newLength, oldLength);

				if (incremental) {
					// Update the old last page + all new pages
					updateRange(oldLength - 1, newLength);
				}
			} else {
				if (incremental) {
					// Only update the last page.
					updateRange(oldLength - 1, oldLength);
				}
			}

			if (!incremental) {
				// Update all pages.
				updateRange(0, newLength);
			}
		}

		public int pageCount() {
			return pages.length;
		}

		public long page2ByteOffset(int page) {
			return ObjectStorage.this.byteOffsetOf(pages[page]);
		}

		public void delete() {
			for (int page : pages) {
				ObjectStorage.this.free(page);
			}
			pages = EMPTY_ALLOCATION;
			modelIndex = -1;
			objectCount = 0;

			ObjectStorage.this.needsUpload = true;
		}

		/**
		 * Calculates the page descriptor for the given page index.
		 * Runs under the assumption than all pages are full except maybe the last one.
		 */
		private int calculatePageDescriptor(int pageIndex) {
			int countInPage;
			if (objectCount % PAGE_SIZE != 0 && pageIndex == pages.length - 1) {
				// Last page && it isn't full -> use the remainder.
				countInPage = objectCount & PAGE_MASK;
			} else if (objectCount > 0) {
				// Full page.
				countInPage = PAGE_SIZE;
			} else {
				// Empty page, this shouldn't be reachable because we eagerly free empty pages.
				countInPage = 0;
			}
			return (modelIndex & 0x3FFFFF) | (countInPage << 26);
		}

		private void updateRange(int start, int oldLength) {
			for (int i = start; i < oldLength; i++) {
				MemoryUtil.memPutInt(ptrForPage(pages[i]), calculatePageDescriptor(i));
			}
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
