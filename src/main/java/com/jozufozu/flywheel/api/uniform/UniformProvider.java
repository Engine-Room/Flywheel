package com.jozufozu.flywheel.api.uniform;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.core.source.FileResolution;

public abstract class UniformProvider {

	protected ByteBuffer buffer;
	protected Notifier notifier;

	public abstract int getSize();

	public void updatePtr(ByteBuffer backing, Notifier notifier) {
		this.buffer = backing;
		this.notifier = notifier;
	}

	public abstract FileResolution getUniformShader();

	public interface Notifier {
		void signalChanged();
	}
}
