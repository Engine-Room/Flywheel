package com.jozufozu.flywheel.lib.task;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.mojang.blaze3d.systems.RenderSystem;

public record OnMainThreadPlan(Runnable task) implements ContextAgnosticPlan {
	public static <C> Plan<C> of(Runnable task) {
		return new OnMainThreadPlan(task).cast();
	}

	@Override
	public void execute(TaskExecutor taskExecutor, Runnable onCompletion) {
		if (RenderSystem.isOnRenderThread()) {
			task.run();
			onCompletion.run();
			return;
		}

		taskExecutor.scheduleForMainThread(() -> {
			task.run();
			onCompletion.run();
		});
	}
}
