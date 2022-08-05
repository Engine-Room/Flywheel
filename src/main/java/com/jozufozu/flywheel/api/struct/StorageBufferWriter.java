package com.jozufozu.flywheel.api.struct;

import com.jozufozu.flywheel.api.instancer.InstancedPart;

public interface StorageBufferWriter<T extends InstancedPart> {

	void write(final long ptr, final T instance, final int batchID);

	int getAlignment();
}
