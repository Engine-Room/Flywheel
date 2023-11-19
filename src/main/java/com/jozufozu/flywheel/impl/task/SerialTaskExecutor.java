package com.jozufozu.flywheel.impl.task;

import java.util.Set;

import com.jozufozu.flywheel.api.task.Flag;
import com.jozufozu.flywheel.api.task.TaskExecutor;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

public class SerialTaskExecutor implements TaskExecutor {
	public static final SerialTaskExecutor INSTANCE = new SerialTaskExecutor();

	private final Set<Flag> flags = new ReferenceOpenHashSet<>();

	private SerialTaskExecutor() {
	}

	@Override
	public void execute(Runnable task) {
		task.run();
	}

	@Override
	public void scheduleForSync(Runnable runnable) {
		runnable.run();
	}

	@Override
	public void syncPoint() {
	}

	@Override
	public boolean syncTo(Flag flag) {
		return isRaised(flag);
	}

	@Override
	public void raise(Flag flag) {
		flags.add(flag);
	}

	@Override
	public void lower(Flag flag) {
		flags.remove(flag);
	}

	@Override
	public boolean isRaised(Flag flag) {
		return flags.contains(flag);
	}

	@Override
	public int getThreadCount() {
		return 1;
	}
}
