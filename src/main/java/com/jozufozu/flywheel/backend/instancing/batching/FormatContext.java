package com.jozufozu.flywheel.backend.instancing.batching;

public record FormatContext(boolean usesOverlay) {

	public static FormatContext defaultContext() {
		return new FormatContext(false);
	}
}
