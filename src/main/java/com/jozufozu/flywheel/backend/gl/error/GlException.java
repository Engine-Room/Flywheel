package com.jozufozu.flywheel.backend.gl.error;

public class GlException extends RuntimeException {

	final GlError errorCode;

	@Override
	public String toString() {
		String s = getClass().getName();
		String message = getLocalizedMessage();
		String withCode = s + ": " + errorCode;
		return (message != null) ? (withCode + ": " + message) : withCode;
	}

	public GlException(GlError errorCode) {
		this.errorCode = errorCode;
	}

	public GlException(GlError errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public GlException(GlError errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public GlException(GlError errorCode, Throwable cause) {
		super(cause);
		this.errorCode = errorCode;
	}

	public GlException(GlError errorCode, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.errorCode = errorCode;
	}
}
