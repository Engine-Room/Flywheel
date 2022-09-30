package com.jozufozu.flywheel.api.uniform;

import com.jozufozu.flywheel.core.source.FileResolution;

public interface UniformProvider {

	int byteSize();

	FileResolution uniformShader();

	ActiveUniformProvider activate(long ptr, Notifier notifier);

	interface ActiveUniformProvider {
		void delete();

		void poll();
	}

	interface Notifier {
		void signalChanged();
	}
}
