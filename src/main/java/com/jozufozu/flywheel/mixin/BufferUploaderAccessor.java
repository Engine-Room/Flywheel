package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.vertex.BufferUploader;

import net.minecraft.client.renderer.ShaderInstance;

@Mixin(BufferUploader.class)
public interface BufferUploaderAccessor {
	@Accessor("lastVertexArrayObject")
	static void flywheel$setLastVAO(int id) {
		throw new AssertionError();
	}
}
