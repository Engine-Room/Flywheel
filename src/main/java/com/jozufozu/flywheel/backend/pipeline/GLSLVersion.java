package com.jozufozu.flywheel.backend.pipeline;

public enum GLSLVersion {
	V110(110),
	V120(120),
	;

	public final int version;

	GLSLVersion(int version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return Integer.toString(version);
	}
}
