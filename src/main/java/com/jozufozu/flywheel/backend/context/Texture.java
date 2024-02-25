package com.jozufozu.flywheel.backend.context;

import com.jozufozu.flywheel.api.BackendImplemented;

@BackendImplemented
public interface Texture {
	void filter(boolean blur, boolean mipmap);
}
