package com.jozufozu.flywheel.lib.task;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.jozufozu.flywheel.api.task.Plan;

public class PlanSimplificationTest {

	public static final Runnable NOOP = () -> {
	};
	public static final Plan SIMPLE = SimplePlan.of(NOOP);

	@Test
	void emptyPlans() {
		var empty = NestedPlan.of();
		Assertions.assertEquals(empty.maybeSimplify(), UnitPlan.INSTANCE);

		var simpleEmpty = SimplePlan.of();
		Assertions.assertEquals(simpleEmpty.maybeSimplify(), UnitPlan.INSTANCE);
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

		var barrier = new BarrierPlan(SIMPLE, SIMPLE);
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
		var onlyUnit = NestedPlan.of(UnitPlan.INSTANCE, UnitPlan.INSTANCE, UnitPlan.INSTANCE);
		Assertions.assertEquals(onlyUnit.maybeSimplify(), UnitPlan.INSTANCE);

		var unitAndSimple = NestedPlan.of(UnitPlan.INSTANCE, UnitPlan.INSTANCE, SIMPLE);
		Assertions.assertEquals(unitAndSimple.maybeSimplify(), SIMPLE);
	}

	@Test
	void complexNesting() {
		var mainThreadNoop = new OnMainThreadPlan(NOOP);

		var nested = NestedPlan.of(mainThreadNoop, SIMPLE);
		Assertions.assertEquals(nested.maybeSimplify(), nested); // cannot simplify

		var barrier = new BarrierPlan(SIMPLE, SIMPLE);
		var complex = NestedPlan.of(barrier, nested);
		Assertions.assertEquals(complex.maybeSimplify(), NestedPlan.of(barrier, mainThreadNoop, SIMPLE));
	}

	@Test
	void nestedNoSimple() {
		var mainThreadNoop = new OnMainThreadPlan(NOOP);
		var barrier = new BarrierPlan(SIMPLE, SIMPLE);
		var oneMainThread = NestedPlan.of(mainThreadNoop, NestedPlan.of(mainThreadNoop, barrier, barrier));

		Assertions.assertEquals(oneMainThread.maybeSimplify(), NestedPlan.of(mainThreadNoop, mainThreadNoop, barrier, barrier));
	}

	@Test
	void manyNestedButJustOneAfterSimplification() {
		var barrier = new BarrierPlan(SIMPLE, SIMPLE);
		var oneMainThread = NestedPlan.of(barrier, NestedPlan.of(UnitPlan.INSTANCE, UnitPlan.INSTANCE));

		Assertions.assertEquals(oneMainThread.maybeSimplify(), barrier);
	}

	@Test
	void barrierPlan() {
		var doubleUnit = new BarrierPlan(UnitPlan.INSTANCE, UnitPlan.INSTANCE);
		Assertions.assertEquals(doubleUnit.maybeSimplify(), UnitPlan.INSTANCE);

		var simpleThenUnit = new BarrierPlan(SIMPLE, UnitPlan.INSTANCE);
		Assertions.assertEquals(simpleThenUnit.maybeSimplify(), SIMPLE);

		var unitThenSimple = new BarrierPlan(UnitPlan.INSTANCE, SIMPLE);
		Assertions.assertEquals(unitThenSimple.maybeSimplify(), SIMPLE);

		var simpleThenSimple = new BarrierPlan(SIMPLE, SIMPLE);
		Assertions.assertEquals(simpleThenSimple.maybeSimplify(), new BarrierPlan(SIMPLE, SIMPLE));
	}
}
