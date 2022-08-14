package com.jozufozu.flywheel.api.uniform;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.core.source.FileResolution;

public abstract class UniformProvider {

	protected long ptr;
	protected Notifier notifier;

	public abstract int getSize();

	public void updatePtr(long ptr, Notifier notifier) {
		this.ptr = ptr;
		this.notifier = notifier;
	}

	public abstract FileResolution getUniformShader();

	public interface Notifier {
		void signalChanged();
	}
}
