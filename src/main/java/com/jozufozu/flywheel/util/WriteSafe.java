package com.jozufozu.flywheel.util;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;

public interface WriteSafe {
	void write(VecBuffer buf);
}
