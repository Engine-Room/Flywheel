package com.jozufozu.flywheel.backend.gl;

public enum GLSLVersion {
	V110(110),
	V120(120),
	V130(130),
	V140(140),
	V150(150),
	V330(330),
	V400(400),
	V410(410),
	V420(420),
	V430(430),
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
