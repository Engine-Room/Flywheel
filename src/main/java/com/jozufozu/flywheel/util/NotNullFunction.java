package com.jozufozu.flywheel.util;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface NotNullFunction<T, R> {
	@NotNull R apply(T t);
}
