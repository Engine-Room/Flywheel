package com.jozufozu.flywheel.glsl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.jozufozu.flywheel.glsl.error.ErrorBuilder;

public class TestErrorMessages extends TestBase {
	@BeforeAll
	static void disableConsoleColors() {
		ErrorBuilder.CONSOLE_COLORS = false;
	}

	@Test
	void testMissingIncludeMsg() {
		var sources = new MockShaderSources();
		sources.add(FLW_A, """
				#include "flywheel:b.glsl"
				""");

		var aErr = assertErrorAndGetMessage(sources, FLW_A);

		assertEquals("""
				error: could not load shader due to errors in included files
				--> flywheel:a.glsl
				1 | #include "flywheel:b.glsl"
				  |           ^^^^^^^^^^^^^^^
				  | error: could not load "flywheel:b.glsl" due to an IO error
				  | note: Mock source not found""", aErr.build());
	}
}
