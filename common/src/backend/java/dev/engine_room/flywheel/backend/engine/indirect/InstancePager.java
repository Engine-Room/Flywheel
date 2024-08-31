package dev.engine_room.flywheel.backend.engine.indirect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.UnknownNullability;
import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.backend.engine.AbstractArena;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;

public class InstancePager extends AbstractArena {
	// 32 objects per page. Allows for convenient bitsets on the gpu.
	public static final int DEFAULT_PAGE_SIZE_OBJECTS = 5;
	public static final int INITIAL_PAGES_ALLOCATED = 4;

	private final int log2PageSize;
	/**
	 * The number of objects in a page.
	 */
	private final int pageSize;

	private final long objectSizeBytes;

	@UnknownNullability
	private MemoryBlock pageData;

	private final int pageMask;
	public final ResizableStorageArray storage;
	public final ResizableStorageArray pageTable;

	private final List<Allocation> allocations = new ArrayList<>();

	public InstancePager(long objectSizeBytes) {
		this(DEFAULT_PAGE_SIZE_OBJECTS, objectSizeBytes);
	}

	public InstancePager(int log2PageSize, long objectSizeBytes) {
		super((1L << log2PageSize) * objectSizeBytes);
		this.log2PageSize = log2PageSize;
		this.pageSize = 1 << log2PageSize;
		this.pageMask = pageSize - 1;
		this.objectSizeBytes = objectSizeBytes;

		this.storage = new ResizableStorageArray(this.elementSizeBytes);
		this.pageTable = new ResizableStorageArray(Integer.BYTES);
	}

	public Allocation createPage() {
		var out = new Allocation();
		allocations.add(out);
		return out;
	}

	@Override
	public long byteCapacity() {
		return storage.byteCapacity();
	}

	@Override
	protected void resize() {
		if (pageData == null) {
			pageData = MemoryBlock.malloc(INITIAL_PAGES_ALLOCATED * Integer.BYTES);
			storage.ensureCapacity(INITIAL_PAGES_ALLOCATED);
			pageTable.ensureCapacity(INITIAL_PAGES_ALLOCATED);
		} else {
			pageData = pageData.realloc(pageData.size() * 2);
			storage.ensureCapacity(storage.capacity() * 2);
			pageTable.ensureCapacity(pageTable.capacity() * 2);
		}
	}

	public void uploadTable(StagingBuffer stagingBuffer) {
		for (Allocation allocation : allocations) {
			allocation.updatePageTable();
		}
		stagingBuffer.enqueueCopy(pageData.ptr(), pageData.size(), pageTable.handle(), 0);
	}

	public void delete() {
		storage.delete();
		pageTable.delete();
		pageData.free();
	}

	public class Allocation {
		public int[] pages = new int[0];

		private int modelIndex = -1;

		public void modelIndex(int modelIndex) {
			if (this.modelIndex != modelIndex) {
				this.modelIndex = modelIndex;
			}
		}

		private void updatePageTable() {
			var ptr = pageData.ptr();

			int fullPage = (modelIndex & 0x3FFFFF) | 0x8000000;

			for (int page : pages) {
				MemoryUtil.memPutInt(ptr + page * Integer.BYTES, fullPage);
			}
		}

		public void activeCount(int objectCount) {
			var neededPages = object2Page((objectCount + pageMask));

			var oldLength = pages.length;

			if (oldLength > neededPages) {
				shrink(oldLength, neededPages);
			} else if (oldLength < neededPages) {
				grow(neededPages, oldLength);
			}
		}

		private void grow(int neededPages, int oldLength) {
			pages = Arrays.copyOf(pages, neededPages);

			for (int i = oldLength; i < neededPages; i++) {
				pages[i] = InstancePager.this.alloc();
			}
		}

		private void shrink(int oldLength, int neededPages) {
			for (int i = oldLength - 1; i > neededPages; i--) {
				var page = pages[i];
				InstancePager.this.free(page);
				MemoryUtil.memPutInt(pageData.ptr() + page * Integer.BYTES, 0);
			}

			pages = Arrays.copyOf(pages, neededPages);
		}

		public int capacity() {
			return pages.length << log2PageSize;
		}

		public int pageCount() {
			return pages.length;
		}

		public int object2Page(int objectIndex) {
			return objectIndex >> log2PageSize;
		}

		public int page2Object(int pageIndex) {
			return pageIndex << log2PageSize;
		}

		public long page2ByteOffset(int page) {
			return InstancePager.this.byteOffsetOf(pages[page]);
		}
	}
}
