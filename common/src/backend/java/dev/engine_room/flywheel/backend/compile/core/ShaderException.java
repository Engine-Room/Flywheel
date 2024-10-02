package dev.engine_room.flywheel.backend.compile.core;

public class ShaderException extends RuntimeException {
	public ShaderException(String message) {
		super(message);
	}

	public ShaderException(String message, Throwable cause) {
		super(message, cause);
	}

	public ShaderException(Throwable cause) {
		super(cause);
	}

	public static class Link extends ShaderException {
		public Link(String message) {
			super(message);
		}

		public Link(String message, Throwable cause) {
			super(message, cause);
		}

		public Link(Throwable cause) {
			super(cause);
		}
	}

	public static class Compile extends ShaderException {
		public Compile(String message) {
			super(message);
		}

		public Compile(String message, Throwable cause) {
			super(message, cause);
		}

		public Compile(Throwable cause) {
			super(cause);
		}
	}

	public static class Load extends ShaderException {
		public Load(String message) {
			super(message);
		}

		public Load(String message, Throwable cause) {
			super(message, cause);
		}

		public Load(Throwable cause) {
			super(cause);
		}
	}
}
