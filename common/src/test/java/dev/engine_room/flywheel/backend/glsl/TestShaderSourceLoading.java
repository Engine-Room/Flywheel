package dev.engine_room.flywheel.backend.glsl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

import dev.engine_room.flywheel.backend.glsl.parse.Import;

public class TestShaderSourceLoading extends TestBase {
	@Test
	void testSimpleFind() {
		var sources = new MockShaderSources();
		sources.add(FLW_A, "");

		SourceFile file = findAndAssertSuccess(sources, FLW_A);

		assertEquals("", file.finalSource);
	}

	@Test
	void testMissingFileAtRoot() {
		var sources = new MockShaderSources();
		findAndAssertError(LoadError.IOError.class, sources, FLW_A);
	}

	/**
	 * #includes should default to the flywheel namespace since minecraft shaders aren't relevant.
	 */
	@Test
	void testNoNamespace() {
		var sources = new MockShaderSources();
		sources.add(FLW_A, """
				#include "b.glsl"
				""");
		sources.add(FLW_B, "");

		findAndAssertSuccess(sources, FLW_A);
		sources.assertLoaded(FLW_B);
	}

	@Test
	void testMissingInclude() {
		var sources = new MockShaderSources();
		sources.add(FLW_A, """
				#include "flywheel:b.glsl"
				""");

		var aErr = findAndAssertError(LoadError.IncludeError.class, sources, FLW_A);

		var ioErr = assertSimpleNestedErrorsToDepth(LoadError.IOError.class, aErr, 1);
		assertEquals(FLW_B, ioErr.location());
	}

	@Test
	void testMalformedInclude() {
		var sources = new MockShaderSources();
		sources.add(FLW_A, """
				#include "evil - wow"
				""");

		var aErr = findAndAssertError(LoadError.IncludeError.class, sources, FLW_A);

		var malformedInclude = assertSimpleNestedErrorsToDepth(LoadError.MalformedInclude.class, aErr, 1);
		var message = malformedInclude.exception()
				.getMessage();
		assertEquals("Non [a-z0-9/._-] character in path of location: flywheel:evil - wow", message);
	}

	@Test
	void testBasicInclude() {
		var sources = new MockShaderSources();
		sources.add(FLW_A, """
				#include "flywheel:b.glsl"
				""");
		sources.add(FLW_B, "");

		SourceFile a = findAndAssertSuccess(sources, FLW_A);
		sources.assertLoaded(FLW_B);

		var includeB = assertSingletonList(a.imports);
		assertEquals(FLW_B.toString(), includeB.file()
				.toString());

		assertEquals("""

				""", a.finalSource, "Include statements should be elided.");
	}

	@Test
	void testRedundantInclude() {
		var sources = new MockShaderSources();
		sources.add(FLW_A, """
				#include "flywheel:b.glsl"
				#include "flywheel:b.glsl"
				""");
		sources.add(FLW_B, "");

		SourceFile a = findAndAssertSuccess(sources, FLW_A);
		sources.assertLoaded(FLW_B);

		assertEquals(2, a.imports.size());
		for (Import include : a.imports) {
			assertEquals(FLW_B.toString(), include.file()
					.toString());
		}

		assertEquals("""


				""", a.finalSource, "Both include statements should be elided.");

		LoadResult bResult = sources.assertLoaded(FLW_B);
		SourceFile b = assertSuccessAndUnwrap(FLW_B, bResult);

		assertEquals(ImmutableList.of(b), a.included);
	}

	@Test
	void testSelfInclude() {
		var sources = new MockShaderSources();
		sources.add(FLW_A, """
				#include "flywheel:a.glsl"
				""");

		var aErr = findAndAssertError(LoadError.IncludeError.class, sources, FLW_A);

		var shouldBeRecursiveIncludePair = assertSingletonList(aErr.innerErrors());

		var circularDependency = assertInstanceOf(LoadError.CircularDependency.class, shouldBeRecursiveIncludePair.second());
		assertEquals(ImmutableList.of(FLW_A, FLW_A), circularDependency.stack());
		assertEquals(FLW_A, circularDependency.offender());

		assertEquals(FLW_A.toString(), shouldBeRecursiveIncludePair.first()
				.toString());
	}

	@Test
	void test2LayerCircularDependency() {
		var sources = new MockShaderSources();
		sources.add(FLW_A, """
				#include "flywheel:b.glsl"
				""");
		sources.add(FLW_B, """
				#include "flywheel:a.glsl"
				""");

		var aErr = findAndAssertError(LoadError.IncludeError.class, sources, FLW_A);
		sources.assertLoaded(FLW_B);

		var recursiveInclude = assertSimpleNestedErrorsToDepth(LoadError.CircularDependency.class, aErr, 2);
		assertEquals(ImmutableList.of(FLW_A, FLW_B, FLW_A), recursiveInclude.stack());
	}

	@Test
	void test3LayerCircularDependency() {
		var sources = new MockShaderSources();
		sources.add(FLW_A, """
				#include "flywheel:b.glsl"
				""");
		sources.add(FLW_B, """
				#include "flywheel:c.glsl"
				""");
		sources.add(FLW_C, """
				#include "flywheel:a.glsl"
				""");

		var aErr = findAndAssertError(LoadError.IncludeError.class, sources, FLW_A);
		sources.assertLoaded(FLW_B);
		sources.assertLoaded(FLW_C);

		var recursiveInclude = assertSimpleNestedErrorsToDepth(LoadError.CircularDependency.class, aErr, 3);
		assertEquals(ImmutableList.of(FLW_A, FLW_B, FLW_C, FLW_A), recursiveInclude.stack());
	}
}
