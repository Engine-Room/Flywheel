package com.jozufozu.flywheel.backend.gl.error;

public class GlException extends RuntimeException {

	final GlError errorCode;

	public GlException(GlError errorCode, String message) {
		super(updateMessage(errorCode, message));
		this.errorCode = errorCode;
	}

	private static String updateMessage(GlError error, String message) {
		return String.format("%s: %s", error, message);
	}
}
