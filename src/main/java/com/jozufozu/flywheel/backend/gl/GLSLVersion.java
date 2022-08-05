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
	V440(440),
	V450(450),
	V460(460),
	;

	public final int version;

	GLSLVersion(int version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return Integer.toString(version);
	}

	public String getVersionLine() {
		return "#version " + version + '\n';
	}
}
