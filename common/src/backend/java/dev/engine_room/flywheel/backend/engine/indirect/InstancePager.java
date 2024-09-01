package dev.engine_room.flywheel.backend.engine.indirect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.backend.engine.AbstractArena;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;

public class InstancePager extends AbstractArena {
	// 32 objects per page. Allows for convenient bitsets on the gpu.
	public static final int LOG_2_PAGE_SIZE = 5;
	public static final int PAGE_SIZE = 1 << LOG_2_PAGE_SIZE;
	public static final int PAGE_MASK = PAGE_SIZE - 1;

	public static final int INITIAL_PAGES_ALLOCATED = 4;

	private final long objectSizeBytes;

	private MemoryBlock pageData;

	public final ResizableStorageBuffer storage;
	public final ResizableStorageBuffer pageTable;

	private final List<Allocation> allocations = new ArrayList<>();

	public InstancePager(long objectSizeBytes) {
		super(PAGE_SIZE * objectSizeBytes);
		this.objectSizeBytes = objectSizeBytes;

		this.storage = new ResizableStorageBuffer();
		this.pageTable = new ResizableStorageBuffer();

		pageData = MemoryBlock.malloc(INITIAL_PAGES_ALLOCATED * Integer.BYTES);
		storage.ensureCapacity(INITIAL_PAGES_ALLOCATED * elementSizeBytes);
		pageTable.ensureCapacity(INITIAL_PAGES_ALLOCATED * Integer.BYTES);
	}

	public static int object2Page(int objectIndex) {
		return objectIndex >> LOG_2_PAGE_SIZE;
	}

	public static int page2Object(int pageIndex) {
		return pageIndex << LOG_2_PAGE_SIZE;
	}

	public Allocation createPage() {
		var out = new Allocation();
		allocations.add(out);
		return out;
	}

	@Override
	public long byteCapacity() {
		return storage.capacity();
	}

	@Override
	protected void grow() {
		pageData = pageData.realloc(pageData.size() * 2);
		storage.ensureCapacity(storage.capacity() * 2);
		pageTable.ensureCapacity(pageTable.capacity() * 2);
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
		private int activeCount = 0;

		public void modelIndex(int modelIndex) {
			if (this.modelIndex != modelIndex) {
				this.modelIndex = modelIndex;
			}
		}

		private void updatePageTable() {
			if (pages.length == 0) {
				return;
			}

			var ptr = pageData.ptr();

			int fullPage = (modelIndex & 0x3FFFFF) | (32 << 26);

			int remainder = activeCount;

			for (int i = 0; i < pages.length - 1; i++) {
				int page = pages[i];
				MemoryUtil.memPutInt(ptr + page * Integer.BYTES, fullPage);
				remainder -= PAGE_SIZE;
			}

			MemoryUtil.memPutInt(ptr + pages[pages.length - 1] * Integer.BYTES, (modelIndex & 0x3FFFFF) | (remainder << 26));
		}

		public void activeCount(int objectCount) {
			var neededPages = object2Page((objectCount + PAGE_MASK));
			activeCount = objectCount;

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
			for (int i = oldLength - 1; i >= neededPages; i--) {
				var page = pages[i];
				InstancePager.this.free(page);
				MemoryUtil.memPutInt(pageData.ptr() + page * Integer.BYTES, 0);
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
	}
}
