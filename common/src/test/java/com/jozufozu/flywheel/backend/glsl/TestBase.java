package com.jozufozu.flywheel.backend.glsl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import com.jozufozu.flywheel.api.Flywheel;

import net.minecraft.resources.ResourceLocation;

public class TestBase {
	public static final ResourceLocation FLW_A = Flywheel.rl("a.glsl");
	public static final ResourceLocation FLW_B = Flywheel.rl("b.glsl");
	public static final ResourceLocation FLW_C = Flywheel.rl("c.glsl");

	public static <T> T assertSingletonList(List<T> list) {
		assertEquals(1, list.size());
		return list.get(0);
	}

	public static <E extends LoadError> E findAndAssertError(Class<E> clazz, MockShaderSources sources, ResourceLocation loc) {
		var result = sources.find(loc);
		var failure = assertInstanceOf(LoadResult.Failure.class, result);
		return assertInstanceOf(clazz, failure.error());
	}

	static <E extends LoadError> E assertSimpleNestedErrorsToDepth(Class<E> finalErrType, LoadError err, int depth) {
		var includeError = assertInstanceOf(LoadError.IncludeError.class, err);

		var pair = assertSingletonList(includeError.innerErrors());
		for (int i = 1; i < depth; i++) {
			includeError = assertInstanceOf(LoadError.IncludeError.class, pair.second());
			pair = assertSingletonList(includeError.innerErrors());
		}
		return assertInstanceOf(finalErrType, pair.second());
	}

	public static SourceFile findAndAssertSuccess(MockShaderSources sources, ResourceLocation loc) {
		var result = sources.find(loc);
		return assertSuccessAndUnwrap(loc, result);
	}

	public static SourceFile assertSuccessAndUnwrap(ResourceLocation expectedName, LoadResult result) {
		assertInstanceOf(LoadResult.Success.class, result);

		var file = result.unwrap();
		assertNotNull(file);
		assertEquals(expectedName, file.name);
		return file;
	}
}
