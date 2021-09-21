package com.jozufozu.flywheel.backend.gl;

public enum GLSLVersion {
	@Deprecated
	V110(110),
	@Deprecated
	V120(120),
	V150(150),
	V330(330),
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
