package com.jozufozu.flywheel.api.registry;

import java.util.Set;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;

@ApiStatus.NonExtendable
public interface Registry<T> extends Iterable<T> {
	void register(T object);

	<S extends T> S registerAndGet(S object);

	@Unmodifiable
	Set<T> getAll();

	void addFreezeCallback(Consumer<Registry<T>> callback);

	boolean isFrozen();
}
