package com.jozufozu.flywheel.core.source;

public class ShaderLoadingException extends RuntimeException {

	public ShaderLoadingException(String message) {
		super(message);
	}

	public ShaderLoadingException(String message, Throwable cause) {
		super(message, cause);
	}
}
