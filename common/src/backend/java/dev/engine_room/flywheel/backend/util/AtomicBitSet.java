package dev.engine_room.flywheel.backend.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicReference;

import org.jetbrains.annotations.NotNull;

// https://github.com/Netflix/hollow/blob/master/hollow/src/main/java/com/netflix/hollow/core/memory/ThreadSafeBitSet.java
// Refactored to remove unused methods, deduplicate some code segments, and add extra functionality with #forEachSetSpan
public class AtomicBitSet {
	// 1024 bits, 128 bytes, 16 longs per segment
	public static final int DEFAULT_LOG2_SEGMENT_SIZE_IN_BITS = 10;

	private static final long WORD_MASK = 0xffffffffffffffffL;

	// We directly use VarHandle instead of going through AtomicLongArray, both to avoid a layer of indirection
	// and to use atomic bitwise operations which are not exposed by AtomicLongArray.
	private static final VarHandle AA = MethodHandles.arrayElementVarHandle(long[].class);

	private final int numLongsPerSegment;
	private final int log2SegmentSize;
	private final int segmentMask;
	private final AtomicReference<AtomicBitSetSegments> segments;

	public AtomicBitSet() {
		this(DEFAULT_LOG2_SEGMENT_SIZE_IN_BITS);
	}

	public AtomicBitSet(int log2SegmentSizeInBits) {
		this(log2SegmentSizeInBits, 0);
	}

	public AtomicBitSet(int log2SegmentSizeInBits, int numBitsToPreallocate) {
		if (log2SegmentSizeInBits < 6) {
			throw new IllegalArgumentException("Cannot specify fewer than 64 bits in each segment!");
		}

		this.log2SegmentSize = log2SegmentSizeInBits;
		this.numLongsPerSegment = (1 << (log2SegmentSizeInBits - 6));
		this.segmentMask = numLongsPerSegment - 1;

		long numBitsPerSegment = numLongsPerSegment * 64L;
		int numSegmentsToPreallocate = numBitsToPreallocate == 0 ? 1 : (int) (((numBitsToPreallocate - 1) / numBitsPerSegment) + 1);

		segments = new AtomicReference<>(new AtomicBitSetSegments(numSegmentsToPreallocate, numLongsPerSegment));
	}

	public void set(int position, boolean value) {
		if (value) {
			set(position);
		} else {
			clear(position);
		}
	}

	public void set(int position) {
		if (position < 0) {
			return;
		}
		int longPosition = longIndexInSegmentForPosition(position);

		long[] segment = getOrCreateSegmentForPosition(position);

		setOr(segment, longPosition, maskForPosition(position));
	}

	public void clear(int position) {
		if (position < 0) {
			return;
		}

		int longPosition = longIndexInSegmentForPosition(position);
		int segmentIndex = segmentIndexForPosition(position);

		var segments = this.segments.get();

		if (segmentIndex >= segments.numSegments()) {
			// If the segment index is out of bounds, we don't need to do anything.
			return;
		}

		setAnd(segments.getSegment(segmentIndex), longPosition, ~maskForPosition(position));
	}

	public void set(int fromIndex, int toIndex) {
		if (toIndex <= fromIndex) {
			return;
		}

		int firstSegmentIndex = segmentIndexForPosition(fromIndex);
		int toSegmentIndex = segmentIndexForPosition(toIndex - 1);

		var segments = expandToFit(toSegmentIndex);

		int fromLongIndex = longIndexInSegmentForPosition(fromIndex);
		int toLongIndex = longIndexInSegmentForPosition(toIndex - 1);

		long fromLongMask = WORD_MASK << fromIndex;
		long toLongMask = WORD_MASK >>> -toIndex;

		var segment = segments.getSegment(firstSegmentIndex);

		if (firstSegmentIndex == toSegmentIndex) {
			// Case 1: One segment

			if (fromLongIndex == toLongIndex) {
				// Case 1A: One Long
				setOr(segment, fromLongIndex, (fromLongMask & toLongMask));
			} else {
				// Case 1B: Multiple Longs
				// Handle first word
				setOr(segment, fromLongIndex, fromLongMask);

				// Handle intermediate words, if any
				for (int i = fromLongIndex + 1; i < toLongIndex; i++) {
					AA.setRelease(segment, i, WORD_MASK);
				}

				// Handle last word
				setOr(segment, toLongIndex, toLongMask);
			}
		} else {
			// Case 2: Multiple Segments
			// Handle first segment

			// Handle first word
			setOr(segment, fromLongIndex, fromLongMask);

			// Handle trailing words, if any
			for (int i = fromLongIndex + 1; i < numLongsPerSegment; i++) {
				AA.setRelease(segment, i, WORD_MASK);
			}

			// Nuke intermediate segments, if any
			for (int i = firstSegmentIndex + 1; i < toSegmentIndex; i++) {
				segment = segments.getSegment(i);

				for (int j = 0; j < segment.length; j++) {
					AA.setRelease(segment, j, WORD_MASK);
				}
			}

			// Handle last segment
			segment = segments.getSegment(toSegmentIndex);

			// Handle leading words, if any
			for (int i = 0; i < toLongIndex; i++) {
				AA.setRelease(segment, i, WORD_MASK);
			}

			// Handle last word
			setOr(segment, toLongIndex, toLongMask);
		}
	}

	public void clear(int fromIndex, int toIndex) {
		if (toIndex <= fromIndex) {
			return;
		}

		var segments = this.segments.get();
		int numSegments = segments.numSegments();

		int firstSegmentIndex = segmentIndexForPosition(fromIndex);

		if (firstSegmentIndex >= numSegments) {
			return;
		}

		int toSegmentIndex = segmentIndexForPosition(toIndex - 1);

		if (toSegmentIndex >= numSegments) {
			toSegmentIndex = numSegments - 1;
			toIndex = numSegments * (1 << log2SegmentSize);
		}

		int fromLongIndex = longIndexInSegmentForPosition(fromIndex);
		int toLongIndex = longIndexInSegmentForPosition(toIndex - 1);

		long fromLongMask = WORD_MASK << fromIndex;
		long toLongMask = WORD_MASK >>> -toIndex;

		var segment = segments.getSegment(firstSegmentIndex);

		if (firstSegmentIndex == toSegmentIndex) {
			// Case 1: One segment

			if (fromLongIndex == toLongIndex) {
				// Case 1A: One Long
				setAnd(segment, fromLongIndex, ~(fromLongMask & toLongMask));
			} else {
				// Case 1B: Multiple Longs
				// Handle first word
				setAnd(segment, fromLongIndex, ~fromLongMask);

				// Handle intermediate words, if any
				for (int i = fromLongIndex + 1; i < toLongIndex; i++) {
					AA.setRelease(segment, i, 0);
				}

				// Handle last word
				setAnd(segment, toLongIndex, ~toLongMask);
			}
		} else {
			// Case 2: Multiple Segments
			// Handle first segment

			// Handle first word
			setAnd(segment, fromLongIndex, ~fromLongMask);

			// Handle trailing words, if any
			for (int i = fromLongIndex + 1; i < numLongsPerSegment; i++) {
				AA.setRelease(segment, i, 0);
			}

			// Nuke intermediate segments, if any
			for (int i = firstSegmentIndex + 1; i < toSegmentIndex; i++) {
				segment = segments.getSegment(i);

				for (int j = 0; j < segment.length; j++) {
					AA.setRelease(segment, j, 0);
				}
			}

			// Handle last segment
			segment = segments.getSegment(toSegmentIndex);

			// Handle leading words, if any
			for (int i = 0; i < toLongIndex; i++) {
				AA.setRelease(segment, i, 0);
			}

			// Handle last word
			setAnd(segment, toLongIndex, ~toLongMask);
		}
	}

	private void setOr(long[] segment, int indexInSegment, long mask) {
		AA.getAndBitwiseOrRelease(segment, indexInSegment, mask);
	}

	private void setAnd(long[] segment, int indexInSegment, long mask) {
		AA.getAndBitwiseAndRelease(segment, indexInSegment, mask);
	}

	public boolean get(int position) {
		int segmentPosition = segmentIndexForPosition(position);
		int longPosition = longIndexInSegmentForPosition(position);

		long[] segment = segmentForPosition(segmentPosition);

		long mask = maskForPosition(position);

		return (((long) AA.getAcquire(segment, longPosition) & mask) != 0);
	}

	public long maxSetBit() {
		AtomicBitSetSegments segments = this.segments.get();

		int segmentIdx = segments.numSegments() - 1;

		for (; segmentIdx >= 0; segmentIdx--) {
			long[] segment = segments.getSegment(segmentIdx);
			for (int longIdx = segment.length - 1; longIdx >= 0; longIdx--) {
				long l = (long) AA.getAcquire(segment, longIdx);
				if (l != 0) {
					return ((long) segmentIdx << log2SegmentSize) + (longIdx * 64L) + (63 - Long.numberOfLeadingZeros(l));
				}
			}
		}

		return -1;
	}

	public int nextSetBit(int fromIndex) {
		if (fromIndex < 0) {
			throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
		}

		AtomicBitSetSegments segments = this.segments.get();

		int segmentPosition = segmentIndexForPosition(fromIndex);
		if (segmentPosition >= segments.numSegments()) {
			return -1;
		}

		int longPosition = longIndexInSegmentForPosition(fromIndex);
		long[] segment = segments.getSegment(segmentPosition);

		long word = (long) AA.getAcquire(segment, longPosition) & (0xffffffffffffffffL << bitPosInLongForPosition(fromIndex));

		while (true) {
			if (word != 0) {
				return (segmentPosition << (log2SegmentSize)) + (longPosition << 6) + Long.numberOfTrailingZeros(word);
			}
			if (++longPosition > segmentMask) {
				segmentPosition++;
				if (segmentPosition >= segments.numSegments()) {
					return -1;
				}
				segment = segments.getSegment(segmentPosition);
				longPosition = 0;
			}

			word = (long) AA.getAcquire(segment, longPosition);
		}
	}

	public int nextClearBit(int fromIndex) {
		if (fromIndex < 0) {
			throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
		}

		int segmentPosition = segmentIndexForPosition(fromIndex);

		AtomicBitSetSegments segments = this.segments.get();

		if (segmentPosition >= segments.numSegments()) {
			return fromIndex;
		}

		int longPosition = longIndexInSegmentForPosition(fromIndex);
		long[] segment = segments.getSegment(segmentPosition);

		long word = ~((long) AA.getAcquire(segment, longPosition)) & (0xffffffffffffffffL << bitPosInLongForPosition(fromIndex));

		while (true) {
			if (word != 0) {
				return (segmentPosition << (log2SegmentSize)) + (longPosition << 6) + Long.numberOfTrailingZeros(word);
			}
			if (++longPosition > segmentMask) {
				segmentPosition++;
				if (segmentPosition >= segments.numSegments()) {
					return segments.numSegments() << log2SegmentSize + (longPosition << 6);
				}
				segment = segments.getSegment(segmentPosition);
				longPosition = 0;
			}

			word = ~((long) AA.getAcquire(segment, longPosition));
		}
	}


	/**
	 * @return the number of bits which are set in this bit set.
	 */
	public int cardinality() {
		return this.segments.get()
				.cardinality();
	}

	/**
	 * Iterate over each contiguous span of set bits.
	 *
	 * @param consumer The consumer to accept each span.
	 */
	public void forEachSetSpan(BitSpanConsumer consumer) {
		AtomicBitSetSegments segments = this.segments.get();

		int start = -1;
		int end = -1;

		for (int segmentIndex = 0; segmentIndex < segments.numSegments(); segmentIndex++) {
			long[] segment = segments.getSegment(segmentIndex);
			for (int longIndex = 0; longIndex < segment.length; longIndex++) {
				long l = (long) AA.getAcquire(segment, longIndex);
				if (l != 0) {
					// The JIT loves this loop. Trying to be clever by starting from Long.numberOfLeadingZeros(l)
					// causes it to be much slower.
					for (int bitIndex = 0; bitIndex < 64; bitIndex++) {
						if ((l & (1L << bitIndex)) != 0) {
							var position = (segmentIndex << log2SegmentSize) + (longIndex << 6) + bitIndex;
							if (start == -1) {
								start = position;
							}
							end = position;
						} else {
							if (start != -1) {
								consumer.accept(start, end);
								start = -1;
								end = -1;
							}
						}
					}
				} else {
					if (start != -1) {
						consumer.accept(start, end);
						start = -1;
						end = -1;
					}
				}
			}
		}

		if (start != -1) {
			consumer.accept(start, end);
		}
	}

	/**
	 * @return the number of bits which are currently specified by this bit set.  This is the maximum value
	 * to which you might need to iterate, if you were to iterate over all bits in this set.
	 */
	public int currentCapacity() {
		return segments.get()
				.numSegments() * (1 << log2SegmentSize);
	}

	public boolean isEmpty() {
		return segments.get()
				.isEmpty();
	}

	/**
	 * Clear all bits to 0.
	 */
	public void clear() {
		AtomicBitSetSegments segments = this.segments.get();

		for (int i = 0; i < segments.numSegments(); i++) {
			long[] segment = segments.getSegment(i);

			for (int j = 0; j < segment.length; j++) {
				AA.setRelease(segment, j, 0L);
			}
		}
	}

	/**
	 * Which bit in the long the given position resides in.
	 *
	 * @param position The absolute position in the bitset.
	 * @return The bit position in the long.
	 */
	private static int bitPosInLongForPosition(int position) {
		// remainder of div by num bits in long (64)
		return position & 0x3F;
	}

	/**
	 * Which long in the segment the given position resides in.
	 *
	 * @param position The absolute position in the bitset
	 * @return The long position in the segment.
	 */
	private int longIndexInSegmentForPosition(int position) {
		// remainder of div by num bits per segment
		return (position >>> 6) & segmentMask;
	}

	/**
	 * Which segment the given position resides in.
	 *
	 * @param position The absolute position in the bitset
	 * @return The segment index.
	 */
	private int segmentIndexForPosition(int position) {
		// div by num bits per segment
		return position >>> log2SegmentSize;
	}

	private static long maskForPosition(int position) {
		return 1L << bitPosInLongForPosition(position);
	}

	private long[] getOrCreateSegmentForPosition(int position) {
		return segmentForPosition(segmentIndexForPosition(position));
	}

	/**
	 * Get the segment at <code>segmentIndex</code>.  If this segment does not yet exist, create it.
	 *
	 * @param segmentIndex the segment index
	 * @return the segment
	 */
	private long[] segmentForPosition(int segmentIndex) {
		return expandToFit(segmentIndex).getSegment(segmentIndex);
	}

	@NotNull
	private AtomicBitSet.AtomicBitSetSegments expandToFit(int segmentIndex) {
		AtomicBitSetSegments visibleSegments = segments.get();

		while (visibleSegments.numSegments() <= segmentIndex) {
			// Thread safety: newVisibleSegments contains all of the segments from the currently visible segments, plus extra.
			// all of the segments in the currently visible segments are canonical and will not change.
			AtomicBitSetSegments newVisibleSegments = new AtomicBitSetSegments(visibleSegments, segmentIndex + 1, numLongsPerSegment);

			// because we are using a compareAndSet, if this thread "wins the race" and successfully sets this variable, then the segments
			// which are newly defined in newVisibleSegments become canonical.
			if (segments.compareAndSet(visibleSegments, newVisibleSegments)) {
				visibleSegments = newVisibleSegments;
			} else {
				// If we "lose the race" and are growing the AtomicBitset segments larger,
				// then we will gather the new canonical sets from the update which we missed on the next iteration of this loop.
				// Newly defined segments in newVisibleSegments will be discarded, they do not get to become canonical.
				visibleSegments = segments.get();
			}
		}
		return visibleSegments;
	}

	private static class AtomicBitSetSegments {
		private final long[][] segments;

		private AtomicBitSetSegments(int numSegments, int segmentLength) {
			long[][] segments = new long[numSegments][];

			for (int i = 0; i < numSegments; i++) {
				segments[i] = new long[segmentLength];
			}

			// Thread safety: Because this.segments is final, the preceding operations in this constructor are guaranteed to be visible to any
			// other thread which accesses this.segments.
			this.segments = segments;
		}

		private AtomicBitSetSegments(AtomicBitSetSegments copyFrom, int numSegments, int segmentLength) {
			long[][] segments = new long[numSegments][];

			for (int i = 0; i < numSegments; i++) {
				segments[i] = i < copyFrom.numSegments() ? copyFrom.getSegment(i) : new long[segmentLength];
			}

			// see above re: thread-safety of this assignment
			this.segments = segments;
		}

		private int cardinality() {
			int numSetBits = 0;

			for (int i = 0; i < numSegments(); i++) {
				long[] segment = getSegment(i);
				for (int j = 0; j < segment.length; j++) {
					numSetBits += Long.bitCount((long) AA.getAcquire(segment, j));
				}
			}
			return numSetBits;
		}

		private boolean isEmpty() {
			// No need to count all set bits to just check if it's empty.
			// As soon as we encounter a set bit we can early out.
			for (int i = 0; i < numSegments(); i++) {
				long[] segment = getSegment(i);
				for (int j = 0; j < segment.length; j++) {
					if ((long) AA.getAcquire(segment, j) != 0) {
						return false;
					}
				}
			}
			return true;
		}

		public int numSegments() {
			return segments.length;
		}

		public long[] getSegment(int index) {
			return segments[index];
		}

	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AtomicBitSet other)) {
			return false;
		}

		if (other.log2SegmentSize != log2SegmentSize) {
			throw new IllegalArgumentException("Segment sizes must be the same");
		}

		AtomicBitSetSegments thisSegments = this.segments.get();
		AtomicBitSetSegments otherSegments = other.segments.get();

		for (int i = 0; i < thisSegments.numSegments(); i++) {
			long[] thisArray = thisSegments.getSegment(i);
			long[] otherArray = (i < otherSegments.numSegments()) ? otherSegments.getSegment(i) : null;

			for (int j = 0; j < thisArray.length; j++) {
				long thisLong = (long) AA.getAcquire(thisArray, j);
				long otherLong = (otherArray == null) ? 0 : (long) AA.getAcquire(otherArray, j);

				if (thisLong != otherLong) {
					return false;
				}
			}
		}

		for (int i = thisSegments.numSegments(); i < otherSegments.numSegments(); i++) {
			long[] otherArray = otherSegments.getSegment(i);

			for (int j = 0; j < otherArray.length; j++) {
				long l = (long) AA.getAcquire(otherArray, j);

				if (l != 0) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = log2SegmentSize;
		result = 31 * result + Arrays.deepHashCode(segments.get().segments);
		return result;
	}

	/**
	 * @return a new BitSet with same bits set
	 */
	public BitSet toBitSet() {
		BitSet resultSet = new BitSet();
		int ordinal = this.nextSetBit(0);
		while (ordinal != -1) {
			resultSet.set(ordinal);
			ordinal = this.nextSetBit(ordinal + 1);
		}
		return resultSet;
	}

	@Override
	public String toString() {
		return toBitSet().toString();
	}

	@FunctionalInterface
	public interface BitSpanConsumer {
		/**
		 * Consume a span of bits.
		 *
		 * @param startInclusive The first (inclusive) bit in the span.
		 * @param endInclusive   The last (inclusive) bit in the span.
		 */
		void accept(int startInclusive, int endInclusive);
	}
}
