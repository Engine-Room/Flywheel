package dev.engine_room.flywheel.backend.engine.indirect;

import java.util.Arrays;

import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.backend.engine.AbstractArena;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;

public class InstancePager extends AbstractArena {
	// 32 objects per page. Allows for convenient bitsets on the gpu.
	public static final int LOG_2_PAGE_SIZE = 5;
	public static final int PAGE_SIZE = 1 << LOG_2_PAGE_SIZE;
	public static final int PAGE_MASK = PAGE_SIZE - 1;

	public static final int INITIAL_PAGES_ALLOCATED = 4;

	private MemoryBlock pageTableData;
	public final ResizableStorageBuffer objects;
	public final ResizableStorageBuffer pageTable;

	private boolean needsUpload = false;

	public InstancePager(long objectSizeBytes) {
		super(PAGE_SIZE * objectSizeBytes);

		this.objects = new ResizableStorageBuffer();
		this.pageTable = new ResizableStorageBuffer();

		pageTableData = MemoryBlock.malloc(INITIAL_PAGES_ALLOCATED * Integer.BYTES);
		objects.ensureCapacity(INITIAL_PAGES_ALLOCATED * elementSizeBytes);
		pageTable.ensureCapacity(INITIAL_PAGES_ALLOCATED * Integer.BYTES);
	}

	public static int object2Page(int objectIndex) {
		return objectIndex >> LOG_2_PAGE_SIZE;
	}

	public static int page2Object(int pageIndex) {
		return pageIndex << LOG_2_PAGE_SIZE;
	}

	public Allocation createAllocation() {
		return new Allocation();
	}

	@Override
	public long byteCapacity() {
		return objects.capacity();
	}

	@Override
	public void free(int i) {
		super.free(i);
		MemoryUtil.memPutInt(ptrForPage(i), 0);
	}

	@Override
	protected void grow() {
		pageTableData = pageTableData.realloc(pageTableData.size() * 2);
		objects.ensureCapacity(objects.capacity() * 2);
		pageTable.ensureCapacity(pageTable.capacity() * 2);
	}

	public void uploadTable(StagingBuffer stagingBuffer) {
		if (!needsUpload) {
			return;
		}
		// We could be smarter about which spans are uploaded but this thing is so small it's probably not worth it.
		stagingBuffer.enqueueCopy(pageTableData.ptr(), pageTableData.size(), pageTable.handle(), 0);
		needsUpload = false;
	}

	public void delete() {
		objects.delete();
		pageTable.delete();
		pageTableData.free();
	}

	private long ptrForPage(int page) {
		return pageTableData.ptr() + (long) page * Integer.BYTES;
	}

	public class Allocation {
		public static final int[] EMPTY_ALLOCATION = new int[0];
		public int[] pages = EMPTY_ALLOCATION;

		private int modelIndex = -1;
		private int objectCount = 0;

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

		public void update(int modelIndex, int objectCount) {
			boolean incremental = this.modelIndex == modelIndex;

			if (incremental && objectCount == this.objectCount) {
				// Nothing will change.
				return;
			}

			InstancePager.this.needsUpload = true;

			this.modelIndex = modelIndex;
			this.objectCount = objectCount;

			var oldLength = pages.length;
			var newLength = object2Page((objectCount + PAGE_MASK));

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

		private void updateRange(int start, int oldLength) {
			for (int i = start; i < oldLength; i++) {
				MemoryUtil.memPutInt(ptrForPage(pages[i]), calculatePageDescriptor(i));
			}
		}

		private void grow(int neededPages, int oldLength) {
			pages = Arrays.copyOf(pages, neededPages);

			for (int i = oldLength; i < neededPages; i++) {
				var page = InstancePager.this.alloc();
				pages[i] = page;
			}
		}

		private void shrink(int oldLength, int neededPages) {
			for (int i = oldLength - 1; i >= neededPages; i--) {
				var page = pages[i];
				InstancePager.this.free(page);
			}

			pages = Arrays.copyOf(pages, neededPages);
		}

		public int capacity() {
			return pages.length << LOG_2_PAGE_SIZE;
		}

		public int pageCount() {
			return pages.length;
		}

		public long page2ByteOffset(int page) {
			return InstancePager.this.byteOffsetOf(pages[page]);
		}

		public void delete() {
			for (int page : pages) {
				InstancePager.this.free(page);
			}
			pages = EMPTY_ALLOCATION;
			modelIndex = -1;
			objectCount = 0;
		}
	}
}
