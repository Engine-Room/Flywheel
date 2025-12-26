package dev.engine_room.flywheel.backend.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestAtomicBitSet {
	/**
	 * Sane range of segment sizes to test when the inner loop is small.
	 */
	public static final int[] LOG2_SEGMENT_SIZES = {6, 7, 8, 9, 10, 11, 12};
	/**
	 * Sane range of segment sizes to test without getting too large.
	 */
	public static final int[] SMALLER_LOG2_SEGMENT_SIZES = {6, 7, 8};
	public static final int NUM_BITS_TO_SET = 1000;
	public static final int CAPACITY = NUM_BITS_TO_SET * 2;

	@Test
	void testNextClearBit() {
		var segmentLength = 1 << AtomicBitSet.DEFAULT_LOG2_SEGMENT_SIZE_IN_BITS;
		var bitLength = 2 << AtomicBitSet.DEFAULT_LOG2_SEGMENT_SIZE_IN_BITS;
		var bs = new AtomicBitSet(AtomicBitSet.DEFAULT_LOG2_SEGMENT_SIZE_IN_BITS, bitLength);

		Assertions.assertEquals(0, bs.nextClearBit(0));
		Assertions.assertEquals(1, bs.nextClearBit(1));

		Assertions.assertEquals(5000, bs.nextClearBit(5000));

		Assertions.assertTrue(bs.isEmpty());
		bs.set(16);
		Assertions.assertFalse(bs.isEmpty());

		Assertions.assertEquals(0, bs.nextClearBit(0));
		Assertions.assertEquals(17, bs.nextClearBit(16));

		bs.set(segmentLength + 1);

		Assertions.assertEquals(0, bs.nextClearBit(0));
		Assertions.assertEquals(segmentLength + 2, bs.nextClearBit(segmentLength + 1));

		bs.set(bitLength);

		Assertions.assertEquals(0, bs.nextClearBit(0));
		Assertions.assertEquals(bitLength + 1, bs.nextClearBit(bitLength));

		for (int i = 0; i < bitLength; i++) {
			bs.set(i);
		}

		Assertions.assertEquals(bitLength + 1, bs.nextClearBit(0));
	}

	@Test
	void testSetRange() {
		for (int log2SegmentSize : LOG2_SEGMENT_SIZES) {
			var bs = new AtomicBitSet(log2SegmentSize, CAPACITY);

			Assertions.assertEquals(0, bs.cardinality(), "BitSet should be empty initially");
			Assertions.assertTrue(bs.isEmpty());

			bs.set(0, NUM_BITS_TO_SET);

			Assertions.assertEquals(NUM_BITS_TO_SET, bs.cardinality(), "BitSet should have " + NUM_BITS_TO_SET + " bits set");
			Assertions.assertFalse(bs.isEmpty());

			for (int i = 0; i < NUM_BITS_TO_SET; i++) {
				Assertions.assertTrue(bs.get(i), "Bit " + i + " should be set");
			}
		}
	}

	@Test
	void testClearRange() {
		for (int log2SegmentSize : LOG2_SEGMENT_SIZES) {
			var bs = new AtomicBitSet(log2SegmentSize, CAPACITY);

			Assertions.assertEquals(0, bs.cardinality(), "BitSet should be empty initially");
			Assertions.assertTrue(bs.isEmpty());

			// Fill it halfway.
			bs.set(0, NUM_BITS_TO_SET);

			// Clear the middle of the set range.

			var midStart = NUM_BITS_TO_SET / 4;
			var midEnd = NUM_BITS_TO_SET * 3 / 4;

			bs.clear(midStart, midEnd);

			assertRangeClear(bs, midStart, midEnd);
		}
	}

	@Test
	void testSmallRange() {
		// Allocate a single long.
		var bs = new AtomicBitSet(6, 64);

		var fromIndex = 20;
		var toIndex = 40;

		// Fill bits within the long.
		bs.set(fromIndex, toIndex);

		assertRangeSet(bs, fromIndex, toIndex);

		// Just clear everything.
		bs.clear();
		Assertions.assertEquals(0, bs.cardinality(), "BitSet should be empty after clearing");
		Assertions.assertTrue(bs.isEmpty());

		// Should only fill the single long.
		bs.set(0, 64);
		Assertions.assertEquals(64, bs.cardinality(), "BitSet should have 64 bits set after setting all");
		Assertions.assertFalse(bs.isEmpty());

		bs.clear(fromIndex, toIndex);

		assertRangeClear(bs, fromIndex, toIndex);
	}

	@Test
	void testBadInputs() {
		// Allocate a single long.
		var bs = new AtomicBitSet(6, 64);

		bs.set(-5);

		Assertions.assertEquals(0, bs.cardinality(), "set with negative should have no effect");
		Assertions.assertEquals(64, bs.currentCapacity(), "set with negative should have no effect");
		Assertions.assertTrue(bs.isEmpty());

		bs.clear(Integer.MAX_VALUE);

		Assertions.assertEquals(0, bs.cardinality(), "clear with out of bounds index should have no effect");
		Assertions.assertEquals(64, bs.currentCapacity(), "clear with out of bounds index should have no effect");
		Assertions.assertTrue(bs.isEmpty());

		bs.clear(-5);

		Assertions.assertEquals(0, bs.cardinality(), "clear with negative index should have no effect");
		Assertions.assertEquals(64, bs.currentCapacity(), "clear with negative index should have no effect");
		Assertions.assertTrue(bs.isEmpty());
	}

	@Test
	void testQuadraticInputs() {
		for (int log2SegmentSize : SMALLER_LOG2_SEGMENT_SIZES) {
			var bs = new AtomicBitSet(log2SegmentSize);

			// Test up to 3 segments.
			var maxIndex = 3 * (1 << log2SegmentSize);
			for (int fromIndex = 0; fromIndex < maxIndex; fromIndex++) {
				for (int toIndex = 0; toIndex < maxIndex; toIndex++) {
					bs.clear();

					Assertions.assertTrue(bs.isEmpty());
					Assertions.assertEquals(0, bs.cardinality());

					bs.set(fromIndex, toIndex);

					if (toIndex <= fromIndex) {
						Assertions.assertEquals(0, bs.cardinality(), "Setting range with toIndex < fromIndex should not set any bits");
						Assertions.assertTrue(bs.isEmpty());
					} else {
						Assertions.assertFalse(bs.isEmpty());
						assertRangeSet(bs, fromIndex, toIndex);

						Assertions.assertEquals(toIndex - 1, bs.maxSetBit());
						Assertions.assertEquals(toIndex, bs.nextClearBit(fromIndex));
						Assertions.assertEquals(fromIndex, bs.nextSetBit(0));
					}

					// Now fill it completely and clear the range.
					bs.set(0, maxIndex);
					Assertions.assertFalse(bs.isEmpty());

					assertRangeSet(bs, 0, maxIndex);

					bs.clear(fromIndex, toIndex);

					if (toIndex <= fromIndex) {
						Assertions.assertEquals(maxIndex, bs.cardinality(), "Setting range with toIndex < fromIndex should not clear any bits");
					} else {
						assertRangeClear(bs, fromIndex, toIndex);

						Assertions.assertEquals(maxIndex - 1, bs.maxSetBit());
						Assertions.assertEquals(fromIndex, bs.nextClearBit(0));
						Assertions.assertEquals(toIndex, bs.nextSetBit(fromIndex));
					}
				}
			}
		}
	}

	@Test
	void testThreadContention() {
		// This test is designed to stress the AtomicBitSet implementation by
		// performing a large number of set and clear operations in parallel.

		var numWordsToTest = 1024;
		var numBitsPerWord = 64;

		var numTotalBits = numWordsToTest * numBitsPerWord;

		// Run the test many times to ensure stability.
		for (int testIter = 0; testIter < 1024; testIter++) {

			var bs = new AtomicBitSet(7, numTotalBits);

			List<Thread> threads = new ArrayList<>();
			// Each thread will set a single bit in many words of the bit set.
			for (int i = 0; i < 64; i++) {
				final int index = i;
				var thread = new Thread(() -> {
					for (int word = 0; word < numWordsToTest; word++) {
						bs.set(index + word * numBitsPerWord);
					}
				});
				thread.start();
				threads.add(thread);
			}

			// Wait for all threads to finish.
			for (int i = 0; i < threads.size(); ) {
				Thread thread = threads.get(i);
				try {
					thread.join();
					i++;
				} catch (InterruptedException e) {
					// Ignore, try to join again.
				}
			}

			threads.clear();

			Assertions.assertEquals(numTotalBits, bs.cardinality(), "All bits should be set after parallel operations");

			// Same thing, but now clear the bits.
			for (int i = 0; i < 64; i++) {
				final int index = i;
				var thread = new Thread(() -> {
					for (int word = 0; word < numWordsToTest; word++) {
						bs.clear(index + word * numBitsPerWord);
					}
				});
				thread.start();
				threads.add(thread);
			}

			// Wait for all threads to finish.
			for (int i = 0; i < threads.size(); ) {
				Thread thread = threads.get(i);
				try {
					thread.join();
					i++;
				} catch (InterruptedException e) {
					// Ignore, try to join again.
				}
			}

			Assertions.assertEquals(0, bs.cardinality(), "All bits should be cleared after parallel operations");
		}
	}

	/**
	 * Replicates a problematic code segment from IndirectInstancer.
	 */
	@Test
	void testParallelUpdateBug() {
		//  var pages = this.pages.get();
		//
		// 		mergeablePages.clear(pages.length, mergeablePages.currentCapacity() + 1);
		//
		// 		int page = 0;
		// 		while (mergeablePages.cardinality() > 1) {
		// 			page = mergeablePages.nextSetBit(page);
		// 			if (page < 0) {
		// 				break;
		// 			            }
		//
		// 			// Find the next mergeable page.
		// 			int next = mergeablePages.nextSetBit(page + 1);
		// 			if (next < 0) {
		// 				break;
		//            }
		//
		// 			// Try to merge the pages.
		// 			pages[page].takeFrom(pages[next]);
		// 		}

		// Unused, but we'll try to index it to trigger out-of-bounds errors.
		var pages = new int[25];
		var mergeablePages = new AtomicBitSet();

		var bitsToFill = pages.length * 2;

		// First, fill everything.
		mergeablePages.set(0, bitsToFill);

		// Clear the tail.
		mergeablePages.clear(pages.length, mergeablePages.currentCapacity() + 1);

		Assertions.assertFalse(mergeablePages.get(pages.length));

		// Iterate.
		int page = 0;
		while (mergeablePages.cardinality() > 1) {
			page = mergeablePages.nextSetBit(page);
			if (page < 0) {
				break;
			}

			mergeablePages.clear(page);

			pages[page] = page;
		}
	}

	/**
	 * Assert the expected result of a set operation on a range of bits.
	 */
	private static void assertRangeSet(AtomicBitSet bs, int fromIndex, int toIndex) {
		for (int i = 0; i < 64; i++) {
			if (i >= fromIndex && i < toIndex) {
				Assertions.assertTrue(bs.get(i), "Bit " + i + " should be set");
			} else {
				Assertions.assertFalse(bs.get(i), "Bit " + i + " should be cleared");
			}
		}
	}

	/**
	 * Assert the expected result of a clear operation on a range of bits.
	 */
	private static void assertRangeClear(AtomicBitSet bs, int fromIndex, int toIndex) {
		for (int i = 0; i < 64; i++) {
			if (i >= fromIndex && i < toIndex) {
				Assertions.assertFalse(bs.get(i), "Bit " + i + " should be cleared");
			} else {
				Assertions.assertTrue(bs.get(i), "Bit " + i + " should be set");
			}
		}
	}
}
