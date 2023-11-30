package com.jozufozu.flywheel.lib.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.impl.task.ParallelTaskExecutor;
import com.jozufozu.flywheel.lib.task.functional.RunnableWithContext;
import com.jozufozu.flywheel.lib.util.Unit;

import it.unimi.dsi.fastutil.ints.IntArrayList;

class PlanExecutionTest {

	protected static ParallelTaskExecutor EXECUTOR;

	@BeforeEach
	public void setUp() {
		var currentThread = Thread.currentThread();
		EXECUTOR = new ParallelTaskExecutor("PlanTest", () -> currentThread == Thread.currentThread());
		EXECUTOR.startWorkers();
	}

	@AfterEach
	public void tearDown() {
		EXECUTOR.stopWorkers();
		EXECUTOR = null;
	}

	@ParameterizedTest
	@ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
	void testSynchronizer(int countDown) {
		var done = new AtomicBoolean(false);
		var synchronizer = new Synchronizer(countDown, () -> done.set(true));

		for (int i = 0; i < countDown - 1; i++) {
			synchronizer.decrementAndEventuallyRun();
			Assertions.assertFalse(done.get(), "Done early at " + i);
		}

		synchronizer.decrementAndEventuallyRun();
		Assertions.assertTrue(done.get());
	}

	@ParameterizedTest
	@ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
	void simpleBarrierSequencing(int barriers) {
		var sequence = new IntArrayList(barriers + 1);
		var expected = new IntArrayList(barriers + 1);

		Plan<Unit> plan = SimplePlan.of(() -> sequence.add(1));
		expected.add(1);

		for (int i = 0; i < barriers; i++) {
			final int sequenceNum = i + 2;
			expected.add(sequenceNum);
			plan = plan.then(SimplePlan.of(() -> sequence.add(sequenceNum)));
		}

		runAndWait(plan);

		Assertions.assertEquals(expected, sequence);
	}

	@RepeatedTest(10)
	void wideBarrierSequencing() {
		var lock = new Object();
		var sequence = new IntArrayList(8);

		RunnableWithContext.Ignored<Unit> addOne = () -> {
			synchronized (lock) {
				sequence.add(1);
			}
		};
		RunnableWithContext.Ignored<Unit> addTwo = () -> {
			synchronized (lock) {
				sequence.add(2);
			}
		};

		var plan = SimplePlan.of(addOne, addOne, addOne, addOne)
				.then(SimplePlan.of(addTwo, addTwo, addTwo, addTwo));

		runAndWait(plan);

		assertExpectedSequence(sequence, 1, 1, 1, 1, 2, 2, 2, 2);
	}

	@Test
	void simpleNestedPlan() {
		var sequence = new IntArrayList(2);
		var plan = NestedPlan.of(SimplePlan.<Unit>of(() -> sequence.add(1)));
		runAndWait(plan);
		assertExpectedSequence(sequence, 1);
	}

	@Test
	void manyNestedPlans() {
		var counter = new AtomicInteger(0);
		var count4 = NestedPlan.<Unit>of(SimplePlan.of(counter::incrementAndGet, counter::incrementAndGet), SimplePlan.of(counter::incrementAndGet, counter::incrementAndGet));

		runAndWait(count4);
		Assertions.assertEquals(4, counter.get());

		counter.set(0);

		var count8Barrier = NestedPlan.of(count4, count4);
		runAndWait(count8Barrier);
		Assertions.assertEquals(8, counter.get());
	}

	@Test
	void emptyPlansDontCallTheExecutor() {
		var done = new AtomicBoolean(false);

		UnitPlan.of()
				.execute(null, Unit.INSTANCE, () -> done.set(true));

		Assertions.assertTrue(done.get());
		done.set(false);

		SimplePlan.of()
				.execute(null, Unit.INSTANCE, () -> done.set(true));

		Assertions.assertTrue(done.get());
		done.set(false);

		NestedPlan.of()
				.execute(null, Unit.INSTANCE, () -> done.set(true));

		Assertions.assertTrue(done.get());
	}

	@Test
	void ifElsePlan() {
		var branch = new AtomicInteger(0);

		var plan = IfElsePlan.<Boolean>on(b -> b)
				.ifTrue(SimplePlan.of(() -> branch.set(1)))
				.ifFalse(SimplePlan.of(() -> branch.set(2)))
				.plan();

		runAndWait(plan, true);

		Assertions.assertEquals(1, branch.get());

		runAndWait(plan, false);

		Assertions.assertEquals(2, branch.get());
	}

	@Test
	void dynamicNestedPlan() {
		var counter = new AtomicInteger(0);

		List<Plan<Unit>> plans = new ArrayList<>();

		// We'll re-use this same plan but append to the list of plans it executes.
		var plan = DynamicNestedPlan.of(() -> plans);

		runAndWait(plan);

		Assertions.assertEquals(0, counter.get());

		plans.add(SimplePlan.of(counter::incrementAndGet));

		runAndWait(plan);

		Assertions.assertEquals(1, counter.get());

		counter.set(0);

		plans.add(SimplePlan.of(counter::incrementAndGet));
		plans.add(SimplePlan.of(counter::incrementAndGet));

		runAndWait(plan);

		Assertions.assertEquals(3, counter.get());
	}

	@Test
	void mainThreadPlanRunsImmediately() {
		var done = new AtomicBoolean(false);
		var plan = SyncedPlan.of(() -> done.set(true));

		plan.execute(EXECUTOR, Unit.INSTANCE);

		Assertions.assertTrue(done.get());
	}

	@Test
	void mainThreadPlanIsNotCalledOffThread() {
		var done = new AtomicBoolean(false);

		var plan = SyncedPlan.of(() -> {
			done.set(true);
		});

		// call execute from within a worker thread
		EXECUTOR.execute(() -> plan.execute(EXECUTOR, Unit.INSTANCE));

		Assertions.assertFalse(done.get());

		EXECUTOR.syncPoint();

		Assertions.assertTrue(done.get());
	}

	@Test
	void flagPlan() {
		var first = new NamedFlag("ready right away");
		var second = new NamedFlag("ready after we sync");

		var sync = new Synchronizer(2, second::raise);

		RaisePlan.raise(first)
				.execute(EXECUTOR, Unit.INSTANCE, sync);

		Assertions.assertTrue(EXECUTOR.syncUntil(first::isRaised), "First flag should be raised since we submitted a plan that raises it");

		Assertions.assertFalse(EXECUTOR.syncUntil(second::isRaised), "Second flag should not be raised yet.");

		sync.decrementAndEventuallyRun();

		Assertions.assertTrue(EXECUTOR.syncUntil(second::isRaised), "Second flag should be raised since it was raised in sync.");
	}

	@Test
	void longWaitForFlag() {
		var first = new NamedFlag("ready right away");
		var second = new NamedFlag("ready after 2s");

		var plan = RaisePlan.raise(first)
				.then(SimplePlan.of(() -> {
					// sleep to add delay between raising the first flag and raising the second flag
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}))
				.then(RaisePlan.raise(second));

		EXECUTOR.execute(() -> plan.execute(EXECUTOR, Unit.INSTANCE));

		Assertions.assertTrue(EXECUTOR.syncUntil(first::isRaised), "First flag should be raised since we submitted a plan that raises it.");

		Assertions.assertFalse(second.isRaised(), "Second flag should not be raised immediately.");

		Assertions.assertTrue(EXECUTOR.syncUntil(second::isRaised), "Second flag should be raised since we were waiting for it.");
	}

	@Test
	void syncUntilReturnsFlagValue() {
		var flag = new NamedFlag("ready right away");

		Assertions.assertFalse(EXECUTOR.syncUntil(flag::isRaised), "Flag should not be raised yet.");

		flag.raise();

		Assertions.assertTrue(EXECUTOR.syncUntil(flag::isRaised), "Flag should be raised since we raised it manually.");

		flag.lower();

		Assertions.assertFalse(EXECUTOR.syncUntil(flag::isRaised), "Flag should not be raised since we lowered it.");
	}

	private static void assertExpectedSequence(IntArrayList sequence, int... expected) {
		Assertions.assertArrayEquals(expected, sequence.toIntArray());
	}

	public static void runAndWait(Plan<Unit> plan) {
		new TestBarrier<>(plan, Unit.INSTANCE).runAndWait();
	}

	public static <C> void runAndWait(Plan<C> plan, C ctx) {
		new TestBarrier<>(plan, ctx).runAndWait();
	}

	private static final class TestBarrier<C> {
		private final Plan<C> plan;
		private final C ctx;
		private boolean done = false;

		private TestBarrier(Plan<C> plan, C ctx) {
			this.plan = plan;
			this.ctx = ctx;
		}

		public void runAndWait() {
			plan.execute(EXECUTOR, ctx, this::doneWithPlan);

			synchronized (this) {
				// early exit in case the plan is already done for e.g. UnitPlan
				if (done) {
					return;
				}

				try {
					wait();
				} catch (InterruptedException ignored) {
					// noop
				}
			}
		}

		public void doneWithPlan() {
			synchronized (this) {
				done = true;
				notifyAll();
			}
		}
	}
}
