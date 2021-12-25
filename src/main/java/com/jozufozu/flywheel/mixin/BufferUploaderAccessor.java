package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.vertex.BufferUploader;

@Mixin(BufferUploader.class)
public interface BufferUploaderAccessor {
	@Accessor("lastVertexArrayObject")
	static void flywheel$setLastVAO(int id) {
		throw new AssertionError();
	}

	@Accessor("lastVertexBufferObject")
	static void flywheel$setLastVBO(int id) {
		throw new AssertionError();
	}

	@Accessor("lastIndexBufferObject")
	static void flywheel$setLastEBO(int id) {
		throw new AssertionError();
	}
}
