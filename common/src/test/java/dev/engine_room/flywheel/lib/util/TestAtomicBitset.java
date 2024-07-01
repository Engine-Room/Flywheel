package dev.engine_room.flywheel.lib.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestAtomicBitset {

	@Test
	void testNextClearBit() {
		var segmentLength = 1 << AtomicBitSet.DEFAULT_LOG2_SEGMENT_SIZE_IN_BITS;
		var bitLength = 2 << AtomicBitSet.DEFAULT_LOG2_SEGMENT_SIZE_IN_BITS;
		var bs = new AtomicBitSet(AtomicBitSet.DEFAULT_LOG2_SEGMENT_SIZE_IN_BITS, bitLength);

		Assertions.assertEquals(0, bs.nextClearBit(0));
		Assertions.assertEquals(1, bs.nextClearBit(1));

		Assertions.assertEquals(5000, bs.nextClearBit(5000));

		bs.set(16);

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
}
