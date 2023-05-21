package com.jozufozu.flywheel.lib.task;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class WaitGroupTest {
	@Test
	public void testExtraDone() {
		WaitGroup wg = new WaitGroup();
		wg.add();
		wg.done();
		assertThrows(IllegalStateException.class, wg::done);
	}

	@Test
	public void testAddNegative() {
		WaitGroup wg = new WaitGroup();
		assertThrows(IllegalArgumentException.class, () -> wg.add(-1));
	}
}
