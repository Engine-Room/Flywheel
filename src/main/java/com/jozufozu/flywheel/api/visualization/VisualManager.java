package com.jozufozu.flywheel.api.visualization;

public interface VisualManager<T> {
	/**
	 * Get the number of game objects that are currently being visualized.
	 *
	 * @return The visual count.
	 */
	int getVisualCount();

	void queueAdd(T obj);

	void queueRemove(T obj);

	void queueUpdate(T obj);
}
