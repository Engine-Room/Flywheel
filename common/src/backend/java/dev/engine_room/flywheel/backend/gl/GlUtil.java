package dev.engine_room.flywheel.backend.gl;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.systems.GpuDeviceBackend;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.engine_room.flywheel.backend.mixin.GpuDeviceAccessor;

public class GlUtil {
	// TODO: This is bad, really bad for when vulkan comes around
	@Deprecated(forRemoval = true)
	public static GlDevice getGlDevice() {
		GpuDeviceBackend backend = ((GpuDeviceAccessor) RenderSystem.getDevice()).flywheel$getBackend();
		return (GlDevice) backend;
	}
}
