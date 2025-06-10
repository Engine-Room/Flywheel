package dev.engine_room.flywheel.backend.gl;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;

public enum Driver {
	NVIDIA,
	AMD,
	INTEL,
	MESA,
	UNKNOWN;

	public final String vendor = glGetString(GL20C.GL_VENDOR);
	public final String renderer = glGetString(GL20C.GL_RENDERER);
	public final String version = glGetString(GL20C.GL_VERSION);
	public final String extensions = glGetString(GL20C.GL_EXTENSIONS);

	private static String glGetString(int name) {
		String s = GL20.glGetString(name);
		return s != null ? s : "";
	}
}
