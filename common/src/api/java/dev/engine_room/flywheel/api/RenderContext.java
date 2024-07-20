package dev.engine_room.flywheel.api;

import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;

@ApiStatus.NonExtendable
public interface RenderContext {
	LevelRenderer renderer();

	ClientLevel level();

	RenderBuffers buffers();

	PoseStack stack();

	Matrix4f projection();

	Matrix4f viewProjection();

	Camera camera();

	float partialTick();
}
