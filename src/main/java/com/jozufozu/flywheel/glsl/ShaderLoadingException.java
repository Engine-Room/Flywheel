package com.jozufozu.flywheel.glsl;

public class ShaderLoadingException extends RuntimeException {

	public ShaderLoadingException(String message) {
		super(message);
	}

	public ShaderLoadingException(String message, Throwable cause) {
		super(message, cause);
	}
}
