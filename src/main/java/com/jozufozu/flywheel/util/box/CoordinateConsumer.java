package com.jozufozu.flywheel.util.box;

@FunctionalInterface
public interface CoordinateConsumer {
	void consume(int x, int y, int z);
}
