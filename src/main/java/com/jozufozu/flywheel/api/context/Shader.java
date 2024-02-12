package com.jozufozu.flywheel.api.context;

import com.jozufozu.flywheel.api.BackendImplemented;

@BackendImplemented
public interface Shader {
	void setTexture(String glslName, Texture texture);
}
