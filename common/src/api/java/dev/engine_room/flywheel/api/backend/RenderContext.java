package dev.engine_room.flywheel.api.backend;

import org.joml.Matrix4fc;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;

public interface RenderContext {
	LevelRenderer renderer();

	ClientLevel level();

	RenderBuffers buffers();

	Matrix4fc modelView();

	Matrix4fc projection();

	Matrix4fc viewProjection();

	CameraRenderState cameraRenderState();

	LevelRenderState levelRenderState();

	float partialTick();
}
