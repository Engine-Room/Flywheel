package com.jozufozu.flywheel.core;

import net.minecraft.client.Camera;

/**
 * A class tracking which object last had {@link Camera#setup} called on it.
 *
 * @see com.jozufozu.flywheel.mixin.CameraMixin
 */
public class LastActiveCamera {

	private static Camera camera;

	public static void _setActiveCamera(Camera camera) {
		LastActiveCamera.camera = camera;
	}

	public static Camera getActiveCamera() {
		return camera;
	}
}
