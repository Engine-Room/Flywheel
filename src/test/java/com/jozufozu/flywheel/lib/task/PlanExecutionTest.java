package com.jozufozu.flywheel.lib.task;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.impl.task.ParallelTaskExecutor;
import com.jozufozu.flywheel.lib.util.Unit;

import it.unimi.dsi.fastutil.ints.IntArrayList;

class PlanExecutionTest {

	protected static final ParallelTaskExecutor EXECUTOR = new ParallelTaskExecutor("PlanTest");

	@BeforeAll
	public static void setUp() {
		EXECUTOR.startWorkers();
	}

	@AfterAll
	public static void tearDown() {
		EXECUTOR.stopWorkers();
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

		ContextRunnable<Unit> addOne = () -> {
			synchronized (lock) {
				sequence.add(1);
			}
		};
		ContextRunnable<Unit> addTwo = () -> {
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
	void unitPlan() {
		var done = new AtomicBoolean(false);

		UnitPlan.of()
				.execute(null, Unit.INSTANCE, () -> done.set(true));

		Assertions.assertTrue(done.get());
	}

	@Test
	void emptyPlan() {
		var done = new AtomicBoolean(false);

		SimplePlan.of()
				.execute(null, Unit.INSTANCE, () -> done.set(true));
		Assertions.assertTrue(done.get());

		done.set(false);
		NestedPlan.of()
				.execute(null, Unit.INSTANCE, () -> done.set(true));
		Assertions.assertTrue(done.get());
	}

	@Test
	void mainThreadPlan() {
		var done = new AtomicBoolean(false);
		var plan = SyncedPlan.of(() -> done.set(true));

		plan.execute(EXECUTOR, Unit.INSTANCE);

		Assertions.assertFalse(done.get());

		EXECUTOR.syncPoint();

		Assertions.assertTrue(done.get());
	}

	@Test
	void flagPlan() {
		var first = new NamedFlag("ready right away");
		var second = new NamedFlag("ready after we sync");

		var sync = new Synchronizer(2, () -> EXECUTOR.raise(second));

		RaisePlan.raise(first)
				.execute(EXECUTOR, Unit.INSTANCE, sync);

		Assertions.assertTrue(EXECUTOR.syncTo(first), "First flag should be raised since we submitted a plan that raises it");

		Assertions.assertFalse(EXECUTOR.syncTo(second), "Second flag should not be raised yet.");

		sync.decrementAndEventuallyRun();

		Assertions.assertTrue(EXECUTOR.syncTo(second), "Second flag should be raised since it was raised in sync.");
	}

	@Test
	void longWaitForFlag() {
		var first = new NamedFlag("ready right away");
		var second = new NamedFlag("ready after 2s");

		RaisePlan.raise(first)
				.then(SimplePlan.of(() -> {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}))
				.then(RaisePlan.raise(second))
				.execute(EXECUTOR, Unit.INSTANCE);

		Assertions.assertTrue(EXECUTOR.syncTo(first), "First flag should be raised since we submitted a plan that raises it.");

		Assertions.assertFalse(EXECUTOR.isRaised(second), "Second flag should not be raised immediately.");

		Assertions.assertTrue(EXECUTOR.syncTo(second), "Second flag should be raised since we were waiting for it.");
	}

	@Test
	void syncToReturnsExpected() {
		var flag = new NamedFlag("ready right away");

		Assertions.assertFalse(EXECUTOR.syncTo(flag), "Flag should not be raised yet.");

		EXECUTOR.raise(flag);

		Assertions.assertTrue(EXECUTOR.syncTo(flag), "Flag should be raised since we raised it manually.");

		EXECUTOR.lower(flag);

		Assertions.assertFalse(EXECUTOR.syncTo(flag), "Flag should not be raised since we lowered it.");
	}

	@Test
	void flagsAreReferenceEqual() {
		var flagA = new NamedFlag("same");
		var flagB = new NamedFlag("same");

		Assertions.assertNotSame(flagA, flagB, "Flags should not be the same object.");
		Assertions.assertEquals(flagA, flagB, "Flags should be equal.");

		Assertions.assertFalse(EXECUTOR.isRaised(flagA), "Flag A should not be raised yet.");
		Assertions.assertFalse(EXECUTOR.isRaised(flagB), "Flag B should not be raised yet.");

		EXECUTOR.raise(flagA);

		Assertions.assertTrue(EXECUTOR.isRaised(flagA), "Flag A should be raised since we raised it manually.");
		Assertions.assertFalse(EXECUTOR.isRaised(flagB), "Flag B should not be raised yet.");

		EXECUTOR.raise(flagB);

		Assertions.assertTrue(EXECUTOR.isRaised(flagA), "Flag A should be raised since we raised it manually.");
		Assertions.assertTrue(EXECUTOR.isRaised(flagB), "Flag B should be raised since we raised it manually.");

		EXECUTOR.lower(flagA);

		Assertions.assertFalse(EXECUTOR.isRaised(flagA), "Flag A should not be raised since we lowered it.");
		Assertions.assertTrue(EXECUTOR.isRaised(flagB), "Flag B should be raised since we raised it manually.");

		EXECUTOR.lower(flagB);

		Assertions.assertFalse(EXECUTOR.isRaised(flagA), "Flag A should not be raised since we lowered it.");
		Assertions.assertFalse(EXECUTOR.isRaised(flagB), "Flag B should not be raised since we lowered it.");
	}

	private static void assertExpectedSequence(IntArrayList sequence, int... expected) {
		Assertions.assertArrayEquals(expected, sequence.toIntArray());
	}

	public static void runAndWait(Plan<Unit> plan) {
		new TestBarrier(plan).runAndWait();
	}

	private static final class TestBarrier {
		private final Plan<Unit> plan;
		private boolean done = false;

		private TestBarrier(Plan<Unit> plan) {
			this.plan = plan;
		}

		public void runAndWait() {
			plan.execute(EXECUTOR, Unit.INSTANCE, this::doneWithPlan);

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
