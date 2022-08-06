package com.jozufozu.flywheel.backend.instancing.indirect;

import static org.lwjgl.opengl.GL46.*;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.util.joml.FrustumIntersection;

// This should be a push constant :whywheel:
public class FrustumUBO {

	public static final int BUFFER_SIZE = 96;

	private final int ubo;
	private final long clientStorage;

	FrustumUBO() {
		ubo = glCreateBuffers();
		glNamedBufferStorage(ubo, BUFFER_SIZE, GL_DYNAMIC_STORAGE_BIT);
		clientStorage = MemoryUtil.nmemAlloc(BUFFER_SIZE);
	}

	public void update(FrustumIntersection frustum) {
		frustum.getJozuPackedPlanes(clientStorage);
		nglNamedBufferSubData(ubo, 0, BUFFER_SIZE, clientStorage);
	}

	public void bind() {
		glBindBufferBase(GL_UNIFORM_BUFFER, 0, ubo);
	}
}
