package dev.engine_room.flywheel.backend.engine.indirect;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceWriter;
import dev.engine_room.flywheel.backend.engine.AbstractInstancer;
import dev.engine_room.flywheel.backend.engine.InstanceHandleImpl;
import dev.engine_room.flywheel.backend.engine.InstancerKey;
import dev.engine_room.flywheel.backend.util.AtomicBitSet;
import dev.engine_room.flywheel.lib.math.MoreMath;

public class IndirectInstancer<I extends Instance> extends AbstractInstancer<I> {
	private final long instanceStride;
	private final InstanceWriter<I> writer;
	private final List<IndirectDraw> associatedDraws = new ArrayList<>();
	private final Vector4fc boundingSphere;

	private final AtomicReference<InstancePage<I>[]> pages = new AtomicReference<>(pageArray(0));
	/**
	 * The set of pages whose count changed and thus need their descriptor re-uploaded.
	 */
	private final AtomicBitSet validityChanged = new AtomicBitSet();
	/**
	 * The set of pages whose content changed and thus need their instances re-uploaded.
	 * Note that we don't re-upload for deletions, as the memory becomes invalid and masked out by the validity bits.
	 */
	private final AtomicBitSet contentsChanged = new AtomicBitSet();
	/**
	 * The set of pages that are entirely full.
	 * We scan the clear bits of this set when trying to add an instance.
	 */
	private final AtomicBitSet fullPages = new AtomicBitSet();
	/**
	 * The set of mergable pages. A page is mergeable if it is not empty and has 16 or fewer instances.
	 * These constraints are set so that we can guarantee that merging two pages leaves one entirely empty,
	 * but we also don't want to waste work merging into pages that are already empty.
	 */
	private final AtomicBitSet mergeablePages = new AtomicBitSet();

	public ObjectStorage.@UnknownNullability Mapping mapping;

	private int modelIndex = -1;
	private int baseInstance = -1;

	public IndirectInstancer(InstancerKey<I> key, Recreate<I> recreate) {
		super(key, recreate);
		instanceStride = MoreMath.align4(type.layout()
				.byteSize());
		writer = this.type.writer();
		boundingSphere = key.model().boundingSphere();
	}

	@SuppressWarnings("unchecked")
	private static <I extends Instance> InstancePage<I>[] pageArray(int length) {
		return new InstancePage[length];
	}

	@SuppressWarnings("unchecked")
	private static <I extends Instance> I[] instanceArray() {
		return (I[]) new Instance[ObjectStorage.PAGE_SIZE];
	}

	@SuppressWarnings("unchecked")
	private static <I extends Instance> InstanceHandleImpl<I>[] handleArray() {
		return new InstanceHandleImpl[ObjectStorage.PAGE_SIZE];
	}

	private static final class InstancePage<I extends Instance> implements InstanceHandleImpl.State<I> {
		private final IndirectInstancer<I> parent;
		private final int pageNo;
		private final I[] instances;
		// Handles are only read in #takeFrom. It would be nice to avoid tracking these at all.
		private final InstanceHandleImpl<I>[] handles;
		/**
		 * A bitset describing which indices in the instances/handles arrays contain live instances.
		 */
		private final AtomicInteger valid;

		private InstancePage(IndirectInstancer<I> parent, int pageNo) {
			this.parent = parent;
			this.pageNo = pageNo;
			this.instances = instanceArray();
			this.handles = handleArray();
			this.valid = new AtomicInteger(0);
		}

		/**
		 * Attempt to add the given instance/handle to this page.
		 *
		 * @param instance The instance to add
		 * @param handle   The instance's handle
		 * @return true if the instance was added, false if the page is full
		 */
		public boolean add(I instance, InstanceHandleImpl<I> handle) {
			// Thread safety: we loop until we either win the race and add the given instance, or we
			// run out of space because other threads trying to add at the same time.
			while (true) {
				int currentValue = valid.get();
				if (isFull(currentValue)) {
					// The page is full, must search elsewhere
					return false;
				}

				// determine what the new long value will be after we set the appropriate bit.
				int index = Integer.numberOfTrailingZeros(~currentValue);

				int newValue = currentValue | (1 << index);

				// if no other thread has modified the value since we read it, we won the race and we are done.
				if (valid.compareAndSet(currentValue, newValue)) {
					instances[index] = instance;
					handles[index] = handle;
					handle.state = this;
					// Handle index is unique amongst all pages of this instancer.
					handle.index = local2HandleIndex(index);

					parent.contentsChanged.set(pageNo);
					parent.validityChanged.set(pageNo);
					if (isFull(newValue)) {
						// The page is now full, mark it so in the bitset.
						// This is safe because only one bit position changes at a time.
						parent.fullPages.set(pageNo);
					}
					if (isEmpty(currentValue)) {
						// Value we just saw was zero, so since we added something we are now mergeable!
						parent.mergeablePages.set(pageNo);
					} else if (Integer.bitCount(currentValue) == 16) {
						// We just filled the 17th instance, so we are no longer mergeable.
						parent.mergeablePages.clear(pageNo);
					}
					return true;
				}
			}
		}

		private int local2HandleIndex(int index) {
			return (pageNo << ObjectStorage.LOG_2_PAGE_SIZE) + index;
		}

		@Override
		public InstanceHandleImpl.State<I> setChanged(int index) {
			parent.contentsChanged.set(pageNo);
			return this;
		}

		@Override
		public InstanceHandleImpl.State<I> setDeleted(int index) {
			int localIndex = index % ObjectStorage.PAGE_SIZE;

			instances[localIndex] = null;
			handles[localIndex] = null;

			while (true) {
				int currentValue = valid.get();
				int newValue = currentValue & ~(1 << localIndex);

				if (valid.compareAndSet(currentValue, newValue)) {
					parent.validityChanged.set(pageNo);
					if (isEmpty(newValue)) {
						// If we decremented to zero then we're no longer mergeable.
						parent.mergeablePages.clear(pageNo);
					} else if (Integer.bitCount(newValue) == 16) {
						// If we decremented to 16 then we're now mergeable.
						parent.mergeablePages.set(pageNo);
					}
					// Set full page last so that other threads don't race to set the other bitsets.
					parent.fullPages.clear(pageNo);
					return InstanceHandleImpl.Deleted.instance();
				}
			}
		}

		@Override
		public InstanceHandleImpl.State<I> setVisible(InstanceHandleImpl<I> handle, int index, boolean visible) {
			if (visible) {
				return this;
			}

			int localIndex = index % ObjectStorage.PAGE_SIZE;

			return new InstanceHandleImpl.Hidden<>(parent.recreate, instances[localIndex]);
		}

		/**
		 * Only call this on 2 pages that are mergeable.
		 *
		 * @param other The page to take instances from.
		 */
		private void takeFrom(InstancePage<I> other) {
			// Fill the holes in this page with instances from the other page.

			int valid = this.valid.get();
			int otherValid = other.valid.get();

			for (int i = 0; i < ObjectStorage.PAGE_SIZE; i++) {
				int mask = 1 << i;

				// Find set bits in the other page.
				if ((otherValid & mask) == 0) {
					continue;
				}

				int writePos = Integer.numberOfTrailingZeros(~valid);

				instances[writePos] = other.instances[i];
				handles[writePos] = other.handles[i];

				handles[writePos].state = this;
				handles[writePos].index = local2HandleIndex(writePos);

				// Clear out the other page.
				otherValid &= ~mask;
				other.handles[i] = null;
				other.instances[i] = null;

				// Set the bit in this page so we can find the next write position.
				valid |= 1 << writePos;

				// If we're full, we're done.
				if (isFull(valid)) {
					break;
				}
			}

			this.valid.set(valid);
			other.valid.set(otherValid);

			// If the other page was quite empty we may still be mergeable.
			parent.mergeablePages.set(pageNo, isMergeable(valid));

			// We definitely changed the contents and validity of this page.
			parent.contentsChanged.set(pageNo);
			parent.validityChanged.set(pageNo);

			// The other page will end up empty, so the validity changes and it's no longer mergeable.
			// Also clear the changed bit so we don't re-upload the instances.
			parent.contentsChanged.clear(other.pageNo);
			parent.validityChanged.set(other.pageNo);
			parent.mergeablePages.clear(other.pageNo);

			if (isFull(valid)) {
				parent.fullPages.set(pageNo);
			}
		}
	}

	public void addDraw(IndirectDraw draw) {
		associatedDraws.add(draw);
	}

	public List<IndirectDraw> draws() {
		return associatedDraws;
	}

	public void update(int modelIndex, int baseInstance) {
		this.baseInstance = baseInstance;

		var sameModelIndex = this.modelIndex == modelIndex;
		if (sameModelIndex && validityChanged.isEmpty()) {
			// Nothing to do!
			return;
		}

		this.modelIndex = modelIndex;

		var pages = this.pages.get();
		mapping.updateCount(pages.length);

		if (sameModelIndex) {
			// Only need to update the changed pages.
			for (int page = validityChanged.nextSetBit(0); page >= 0 && page < pages.length; page = validityChanged.nextSetBit(page + 1)) {
				mapping.updatePage(page, modelIndex, pages[page].valid.get());
			}
		} else {
			// Need to update all pages since the model index changed.
			for (int i = 0; i < pages.length; i++) {
				mapping.updatePage(i, modelIndex, pages[i].valid.get());
			}
		}

		validityChanged.clear();
	}

	public void writeModel(long ptr) {
		MemoryUtil.memPutInt(ptr, 0); // instanceCount - to be incremented by the cull shader
		MemoryUtil.memPutInt(ptr + 4, baseInstance); // baseInstance
		MemoryUtil.memPutInt(ptr + 8, environment.matrixIndex()); // matrixIndex
		MemoryUtil.memPutFloat(ptr + 12, boundingSphere.x()); // boundingSphere
		MemoryUtil.memPutFloat(ptr + 16, boundingSphere.y());
		MemoryUtil.memPutFloat(ptr + 20, boundingSphere.z());
		MemoryUtil.memPutFloat(ptr + 24, boundingSphere.w());
	}

	public void uploadInstances(StagingBuffer stagingBuffer, int instanceVbo) {
		if (contentsChanged.isEmpty()) {
			return;
		}

		var pages = this.pages.get();
		for (int page = contentsChanged.nextSetBit(0); page >= 0 && page < pages.length; page = contentsChanged.nextSetBit(page + 1)) {
			var instances = pages[page].instances;

			long baseByte = mapping.page2ByteOffset(page);

			if (baseByte < 0) {
				// This page is not mapped to the VBO.
				continue;
			}

			long size = ObjectStorage.PAGE_SIZE * instanceStride;

			// Because writes are broken into pages, we end up with significantly more calls into
			// StagingBuffer#enqueueCopy and the allocations for the writer got out of hand. Here
			// we've inlined the enqueueCopy call and do not allocate the write lambda at all.
			// Doing so cut upload times in half.

			// Try to write directly into the staging buffer if there is enough contiguous space.
			long direct = stagingBuffer.reserveForCopy(size, instanceVbo, baseByte);

			if (direct != MemoryUtil.NULL) {
				for (I instance : instances) {
					if (instance != null) {
						writer.write(direct, instance);
					}
					direct += instanceStride;
				}
				continue;
			}

			// Otherwise, write to a scratch buffer and enqueue a copy.
			var block = stagingBuffer.getScratch(size);
			var ptr = block.ptr();
			for (I instance : instances) {
				if (instance != null) {
					writer.write(ptr, instance);
				}
				ptr += instanceStride;
			}
			stagingBuffer.enqueueCopy(block.ptr(), size, instanceVbo, baseByte);
		}

		contentsChanged.clear();
	}

	public void parallelUpdate() {
		var pages = this.pages.get();

		int page = 0;
		while (mergeablePages.cardinality() > 1) {
			page = mergeablePages.nextSetBit(page);
			if (page < 0) {
				break;
			}

			// Find the next mergeable page.
			int next = mergeablePages.nextSetBit(page + 1);
			if (next < 0) {
				break;
			}

			// Try to merge the pages.
			pages[page].takeFrom(pages[next]);
		}
	}

	private static boolean isFull(int valid) {
		return valid == 0xFFFFFFFF;
	}

	private static boolean isEmpty(int valid) {
		return valid == 0;
	}

	private static boolean isMergeable(int valid) {
		return !isEmpty(valid) && Integer.bitCount(valid) <= 16;
	}

	@Override
	public void delete() {
		for (IndirectDraw draw : draws()) {
			draw.delete();
		}

		mapping.delete();
	}

	public int modelIndex() {
		return modelIndex;
	}

	public int baseInstance() {
		return baseInstance;
	}

	public int local2GlobalInstanceIndex(int instanceIndex) {
		return mapping.objectIndex2GlobalIndex(instanceIndex);
	}

	@Override
	public I createInstance() {
		var handle = new InstanceHandleImpl<I>(null);
		I instance = type.create(handle);

		addInner(instance, handle);

		return instance;
	}

	public InstanceHandleImpl.State<I> revealInstance(InstanceHandleImpl<I> handle, I instance) {
		addInner(instance, handle);
		return handle.state;
	}

	@Override
	public void stealInstance(@Nullable I instance) {
		if (instance == null) {
			return;
		}

		var instanceHandle = instance.handle();

		if (!(instanceHandle instanceof InstanceHandleImpl<?>)) {
			// UB: do nothing
			return;
		}

		// Should InstanceType have an isInstance method?
		@SuppressWarnings("unchecked") var handle = (InstanceHandleImpl<I>) instanceHandle;

		// Not allowed to steal deleted instances.
		if (handle.state instanceof InstanceHandleImpl.Deleted) {
			return;
		}
		// No need to steal if the instance will recreate to us.
		if (handle.state instanceof InstanceHandleImpl.Hidden<I> hidden && recreate.equals(hidden.recreate())) {
			return;
		}

		// FIXME: in theory there could be a race condition here if the instance
		//  is somehow being stolen by 2 different instancers between threads.
		//  That seems kinda impossible so I'm fine leaving it as is for now.

		// Add the instance to this instancer.
		if (handle.state instanceof InstancePage<?> other) {
			if (other.parent == this) {
				return;
			}

			// Remove the instance from its old instancer.
			// This won't have any unwanted effect when the old instancer
			// is filtering deleted instances later, so is safe.
			other.setDeleted(handle.index);

			// Only lock now that we'll be mutating our state.
			addInner(instance, handle);
		} else if (handle.state instanceof InstanceHandleImpl.Hidden<I>) {
			handle.state = new InstanceHandleImpl.Hidden<>(recreate, instance);
		}
	}

	private void addInner(I instance, InstanceHandleImpl<I> handle) {
		// Outer loop:
		// - try to find an empty space
		// - or grow the page array if we can't
		// - add the instance to the new page, or try again
		while (true) {
			var pages = this.pages.get();

			// First, try to find a page with space.
			for (int i = fullPages.nextClearBit(0); i < pages.length; i = fullPages.nextClearBit(i + 1)) {
				// It may have been filled in while we were searching, but hopefully not.
				if (pages[i].add(instance, handle)) {
					return;
				}
			}

			// If we're here, all other pages are full
			// If we hit this on the second iteration of the outer loop then `pages` is once again full.
			var desiredLength = pages.length + 1;

			// Inner loop: grow the page array. This is very similar to the logic in AtomicBitSet.
			while (pages.length < desiredLength) {
				// Thread safety: segments contains all pages from the currently visible pages, plus extra.
				// all pages in the currently visible pages are canonical and will not change.
				// Can't just `new InstancePage[]` because it has a generic parameter.
				InstancePage<I>[] newPages = pageArray(desiredLength);

				System.arraycopy(pages, 0, newPages, 0, pages.length);
				newPages[pages.length] = new InstancePage<>(this, pages.length);

				// because we are using a compareAndSet, if this thread "wins the race" and successfully sets this variable, then the new page becomes canonical.
				if (this.pages.compareAndSet(pages, newPages)) {
					pages = newPages;
				} else {
					// If we "lose the race" and are growing the AtomicBitset segments larger,
					// then we will gather the new canonical pages from the update which we missed on the next iteration of this loop.
					// The new page will be discarded and never seen again.
					pages = this.pages.get();
				}
			}

			// Shortcut: try to add the instance to the last page.
			// Technically we could just let the outer loop go again, but that
			// involves a good bit of work just to likely get back here.
			if (pages[pages.length - 1].add(instance, handle)) {
				return;
			}
			// It may be the case that many other instances were added in the same instant.
			// We can still lose this race, though it is very unlikely.
		}
	}

	public int instanceCount() {
		// Not exactly accurate but it's an upper bound.
		// TODO: maybe this could be tracked with an AtomicInteger?
		return pages.get().length << ObjectStorage.LOG_2_PAGE_SIZE;
	}

	/**
	 * Clear all instances without freeing resources.
	 */
	public void clear() {
		this.pages.set(pageArray(0));
		contentsChanged.clear();
		validityChanged.clear();
		fullPages.clear();
		mergeablePages.clear();
	}
}
