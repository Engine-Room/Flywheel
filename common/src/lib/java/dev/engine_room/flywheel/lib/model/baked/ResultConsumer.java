package dev.engine_room.flywheel.lib.model.baked;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.renderer.RenderType;

interface ResultConsumer<T> {
	@Nullable
	T createKey(RenderType renderType, boolean shade, boolean ambientOcclusion);

	void accept(T key, BufferBuilder.RenderedBuffer data);
}
