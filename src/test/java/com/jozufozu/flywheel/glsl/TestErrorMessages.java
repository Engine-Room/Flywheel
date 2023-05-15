package com.jozufozu.flywheel.glsl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.jozufozu.flywheel.glsl.error.ErrorBuilder;

import net.minecraft.resources.ResourceLocation;

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

		assertErrorMatches("""
				error: could not load "flywheel:a.glsl"
				--> flywheel:a.glsl
				1 | #include "flywheel:b.glsl"
				  |           ^^^^^^^^^^^^^^^
				  = error: "flywheel:b.glsl" was not found
				""", sources, FLW_A);
	}

	@Test
	void testNestedIncludeMsg() {
		var sources = new MockShaderSources();
		sources.add(FLW_A, """
				#include "flywheel:b.glsl"
				""");
		sources.add(FLW_B, """
				#include "flywheel:c.glsl"
				""");

		assertErrorMatches("""
				error: could not load "flywheel:a.glsl"
				--> flywheel:a.glsl
				1 | #include "flywheel:b.glsl"
				  |           ^^^^^^^^^^^^^^^
				  = error: could not load "flywheel:b.glsl"
				  = --> flywheel:b.glsl
				  = 1 | #include "flywheel:c.glsl"
				  =   |           ^^^^^^^^^^^^^^^
				  =   = error: "flywheel:c.glsl" was not found
				""", sources, FLW_A);
	}

	public static void assertErrorMatches(String expected, MockShaderSources sources, ResourceLocation loc) {
		var message = assertErrorAndGetMessage(sources, loc).build();

		assertEquals(expected.trim(), message.trim());
	}

	@NotNull
	public static ErrorBuilder assertErrorAndGetMessage(MockShaderSources sources, ResourceLocation loc) {
		var result = sources.find(loc);
		var failure = assertInstanceOf(LoadResult.Failure.class, result);
		return failure.error()
				.generateMessage();
	}
}
