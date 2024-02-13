package com.jozufozu.flywheel.api.context;

import org.joml.Matrix4fc;

import com.jozufozu.flywheel.api.BackendImplemented;

@BackendImplemented
public interface Shader {
	void setTexture(String glslName, Texture texture);

	void setFloat(String glslName, float value);

	void setVec2(String glslName, float x, float y);

	void setVec3(String glslName, float x, float y, float z);

	void setVec4(String glslName, float x, float y, float z, float w);

	void setMat4(String glslName, Matrix4fc matrix);
}
