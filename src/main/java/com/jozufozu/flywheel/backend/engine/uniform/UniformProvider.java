package com.jozufozu.flywheel.backend.engine.uniform;

public interface UniformProvider {
	void write(long ptr);

	int byteSize();
}
