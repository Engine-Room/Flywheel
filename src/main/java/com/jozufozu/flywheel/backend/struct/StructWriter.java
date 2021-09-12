package com.jozufozu.flywheel.backend.struct;

public interface StructWriter<S> {

	void write(S struct);

	void seek(int pos);
}
