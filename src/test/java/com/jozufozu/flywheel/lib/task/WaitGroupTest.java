package com.jozufozu.flywheel.lib.task;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WaitGroupTest {
	@Test
	public void testExtraDone() {
		WaitGroup wg = new WaitGroup();
		wg.add();
		wg.done();
		Assertions.assertThrows(IllegalStateException.class, wg::done);
	}
}
