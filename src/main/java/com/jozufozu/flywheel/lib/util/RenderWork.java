package com.jozufozu.flywheel.lib.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraftforge.client.event.RenderLevelLastEvent;

public class RenderWork {
	private static final Queue<Runnable> RUNS = new ConcurrentLinkedQueue<>();

	public static void onRenderLevelLast(RenderLevelLastEvent event) {
		while (!RUNS.isEmpty()) {
			RUNS.remove()
					.run();
		}
	}

	/**
	 * Queue work to be executed at the end of a frame
	 */
	public static void enqueue(Runnable run) {
		RUNS.add(run);
	}
}
