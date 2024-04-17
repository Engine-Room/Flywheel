package com.jozufozu.flywheel.lib.task;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.lib.util.Unit;

public class PlanCompositionTest {

	public static final Plan<Unit> SIMPLE = SimplePlan.of(() -> {
	});

	@Test
	void nestedPlanAnd() {
		var empty = NestedPlan.of(SIMPLE);

		Assertions.assertEquals(NestedPlan.of(SIMPLE, SIMPLE), empty.and(SIMPLE));
	}
}
