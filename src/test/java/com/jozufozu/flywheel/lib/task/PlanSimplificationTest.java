package com.jozufozu.flywheel.lib.task;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.util.Unit;

public class PlanSimplificationTest {

	public static final Runnable NOOP = () -> {
	};
	public static final Plan<Unit> SIMPLE = SimplePlan.of(NOOP);

	@Test
	void emptyPlans() {
		var empty = NestedPlan.of();
		Assertions.assertEquals(empty.maybeSimplify(), UnitPlan.of());

		var simpleEmpty = SimplePlan.of();
		Assertions.assertEquals(simpleEmpty.maybeSimplify(), UnitPlan.of());
	}

	@Test
	void nestedSimplePlans() {
		var twoSimple = NestedPlan.of(SimplePlan.of(NOOP, NOOP, NOOP), SIMPLE);
		Assertions.assertEquals(twoSimple.maybeSimplify(), SimplePlan.of(NOOP, NOOP, NOOP, NOOP));

		var threeSimple = NestedPlan.of(SIMPLE, SIMPLE, SIMPLE);
		Assertions.assertEquals(threeSimple.maybeSimplify(), SimplePlan.of(NOOP, NOOP, NOOP));
	}

	@Test
	void oneNestedPlan() {
		var oneSimple = NestedPlan.of(SIMPLE);

		Assertions.assertEquals(oneSimple.maybeSimplify(), SIMPLE);

		var mainThreadNoop = new OnMainThreadPlan(NOOP);
		var oneMainThread = NestedPlan.of(mainThreadNoop);

		Assertions.assertEquals(oneMainThread.maybeSimplify(), mainThreadNoop);

		var barrier = new BarrierPlan<>(SIMPLE, SIMPLE);
		var oneBarrier = NestedPlan.of(barrier);

		Assertions.assertEquals(oneBarrier.maybeSimplify(), barrier);
	}

	@Test
	void nestedNestedPlan() {
		var outer = NestedPlan.of(SIMPLE);
		var outermost = NestedPlan.of(outer);

		Assertions.assertEquals(outermost.maybeSimplify(), SIMPLE);
	}

	@Test
	void nestedUnitPlan() {
		var onlyUnit = NestedPlan.of(UnitPlan.of(), UnitPlan.of(), UnitPlan.of());
		Assertions.assertEquals(onlyUnit.maybeSimplify(), UnitPlan.of());

		var unitAndSimple = NestedPlan.of(UnitPlan.of(), UnitPlan.of(), SIMPLE);
		Assertions.assertEquals(unitAndSimple.maybeSimplify(), SIMPLE);
	}

	@Test
	void complexNesting() {
		var mainThreadNoop = OnMainThreadPlan.<Unit>of(NOOP);

		var nested = NestedPlan.of(mainThreadNoop, SIMPLE);
		Assertions.assertEquals(nested.maybeSimplify(), nested); // cannot simplify

		var barrier = new BarrierPlan<>(SIMPLE, SIMPLE);
		var complex = NestedPlan.of(barrier, nested);
		Assertions.assertEquals(complex.maybeSimplify(), NestedPlan.of(barrier, mainThreadNoop, SIMPLE));
	}

	@Test
	void nestedNoSimple() {
		var mainThreadNoop = OnMainThreadPlan.<Unit>of(NOOP);
		var barrier = new BarrierPlan<>(SIMPLE, SIMPLE);
		var oneMainThread = NestedPlan.of(mainThreadNoop, NestedPlan.of(mainThreadNoop, barrier, barrier));

		Assertions.assertEquals(oneMainThread.maybeSimplify(), NestedPlan.of(mainThreadNoop, mainThreadNoop, barrier, barrier));
	}

	@Test
	void manyNestedButJustOneAfterSimplification() {
		var barrier = new BarrierPlan<>(SIMPLE, SIMPLE);
		var oneMainThread = NestedPlan.of(barrier, NestedPlan.of(UnitPlan.of(), UnitPlan.of()));

		Assertions.assertEquals(oneMainThread.maybeSimplify(), barrier);
	}

	@Test
	void barrierPlan() {
		var doubleUnit = new BarrierPlan<>(UnitPlan.of(), UnitPlan.of());
		Assertions.assertEquals(doubleUnit.maybeSimplify(), UnitPlan.of());

		var simpleThenUnit = new BarrierPlan<>(SIMPLE, UnitPlan.of());
		Assertions.assertEquals(simpleThenUnit.maybeSimplify(), SIMPLE);

		var unitThenSimple = new BarrierPlan<>(UnitPlan.of(), SIMPLE);
		Assertions.assertEquals(unitThenSimple.maybeSimplify(), SIMPLE);

		var simpleThenSimple = new BarrierPlan<>(SIMPLE, SIMPLE);
		Assertions.assertEquals(simpleThenSimple.maybeSimplify(), new BarrierPlan<>(SIMPLE, SIMPLE));
	}
}
