package com.jozufozu.flywheel.lib.task;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.jozufozu.flywheel.api.task.Plan;

public class PlanCompositionTest {

	public static final Runnable NOOP = () -> {
	};
	public static final Plan SIMPLE = SimplePlan.of(NOOP);

	@Test
	void nestedPlanAnd() {
		var empty = NestedPlan.of(SIMPLE);

		Assertions.assertEquals(NestedPlan.of(SIMPLE, SIMPLE), empty.and(SIMPLE));
	}
}
