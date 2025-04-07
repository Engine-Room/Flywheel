package dev.engine_room.vanillin.text;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.common.collect.Queues;

public class AsyncFontUploads {
	private static final ConcurrentLinkedQueue<FontTextureExtension> schedule = Queues.newConcurrentLinkedQueue();

	public static void push(FontTextureExtension flush) {
		schedule.add(flush);
	}

	public static void execute() {
		FontTextureExtension poll;
		while ((poll = schedule.poll()) != null) {
			poll.flywheel$flush();
		}
	}
}
