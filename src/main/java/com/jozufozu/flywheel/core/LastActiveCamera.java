package com.jozufozu.flywheel.core;

import net.minecraft.client.Camera;

public class LastActiveCamera {

	private static Camera camera;

	public static void _setActiveCamera(Camera camera) {
		LastActiveCamera.camera = camera;
	}

	public static Camera getActiveCamera() {
		return camera;
	}
}
