package com.jozufozu.flywheel.lib.task;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.lib.task.functional.RunnableWithContext;
import com.jozufozu.flywheel.lib.util.Unit;

public class PlanSimplificationTest {

	public static final RunnableWithContext.Ignored<Unit> NOOP = () -> {
	};
	public static final Plan<Unit> SIMPLE = SimplePlan.of(NOOP);

	@Test
	void emptyPlans() {
		var empty = NestedPlan.of();
		Assertions.assertEquals(empty.simplify(), UnitPlan.of());

		var simpleEmpty = SimplePlan.of();
		Assertions.assertEquals(simpleEmpty.simplify(), UnitPlan.of());
	}

	@Test
	void nestedSimplePlans() {
		var twoSimple = NestedPlan.of(SimplePlan.of(NOOP, NOOP, NOOP), SIMPLE);
		Assertions.assertEquals(twoSimple.simplify(), SimplePlan.of(NOOP, NOOP, NOOP, NOOP));

		var threeSimple = NestedPlan.of(SIMPLE, SIMPLE, SIMPLE);
		Assertions.assertEquals(threeSimple.simplify(), SimplePlan.of(NOOP, NOOP, NOOP));
	}

	@Test
	void oneNestedPlan() {
		var oneSimple = NestedPlan.of(SIMPLE);

		Assertions.assertEquals(oneSimple.simplify(), SIMPLE);

		var mainThreadNoop = new SyncedPlan<>(NOOP);
		var oneMainThread = NestedPlan.of(mainThreadNoop);

		Assertions.assertEquals(oneMainThread.simplify(), mainThreadNoop);

		var barrier = new BarrierPlan<>(SIMPLE, SIMPLE);
		var oneBarrier = NestedPlan.of(barrier);

		Assertions.assertEquals(oneBarrier.simplify(), barrier);
	}

	@Test
	void nestedNestedPlan() {
		var outer = NestedPlan.of(SIMPLE);
		var outermost = NestedPlan.of(outer);

		Assertions.assertEquals(outermost.simplify(), SIMPLE);
	}

	@Test
	void nestedUnitPlan() {
		var onlyUnit = NestedPlan.of(UnitPlan.of(), UnitPlan.of(), UnitPlan.of());
		Assertions.assertEquals(onlyUnit.simplify(), UnitPlan.of());

		var unitAndSimple = NestedPlan.of(UnitPlan.of(), UnitPlan.of(), SIMPLE);
		Assertions.assertEquals(unitAndSimple.simplify(), SIMPLE);
	}

	@Test
	void complexNesting() {
		var mainThreadNoop = SyncedPlan.<Unit>of(() -> {
		});

		var nested = NestedPlan.of(mainThreadNoop, SIMPLE);
		Assertions.assertEquals(nested.simplify(), nested); // cannot simplify

		var barrier = new BarrierPlan<>(SIMPLE, SIMPLE);
		var complex = NestedPlan.of(barrier, nested);
		Assertions.assertEquals(complex.simplify(), NestedPlan.of(barrier, mainThreadNoop, SIMPLE));
	}

	@Test
	void nestedNoSimple() {
		var mainThreadNoop = SyncedPlan.<Unit>of(() -> {
		});
		var barrier = new BarrierPlan<>(SIMPLE, SIMPLE);
		var oneMainThread = NestedPlan.of(mainThreadNoop, NestedPlan.of(mainThreadNoop, barrier, barrier));

		Assertions.assertEquals(oneMainThread.simplify(), NestedPlan.of(mainThreadNoop, mainThreadNoop, barrier, barrier));
	}

	@Test
	void manyNestedButJustOneAfterSimplification() {
		var barrier = new BarrierPlan<>(SIMPLE, SIMPLE);
		var oneMainThread = NestedPlan.of(barrier, NestedPlan.of(UnitPlan.of(), UnitPlan.of()));

		Assertions.assertEquals(oneMainThread.simplify(), barrier);
	}

	@Test
	void barrierPlan() {
		var doubleUnit = new BarrierPlan<>(UnitPlan.of(), UnitPlan.of());
		Assertions.assertEquals(doubleUnit.simplify(), UnitPlan.of());

		var simpleThenUnit = new BarrierPlan<>(SIMPLE, UnitPlan.of());
		Assertions.assertEquals(simpleThenUnit.simplify(), SIMPLE);

		var unitThenSimple = new BarrierPlan<>(UnitPlan.of(), SIMPLE);
		Assertions.assertEquals(unitThenSimple.simplify(), SIMPLE);

		var simpleThenSimple = new BarrierPlan<>(SIMPLE, SIMPLE);
		Assertions.assertEquals(simpleThenSimple.simplify(), new BarrierPlan<>(SIMPLE, SIMPLE));
	}
}
