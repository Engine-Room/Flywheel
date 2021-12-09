package com.jozufozu.flywheel.light;

@FunctionalInterface
public interface CoordinateConsumer {
	void consume(int x, int y, int z);
}
