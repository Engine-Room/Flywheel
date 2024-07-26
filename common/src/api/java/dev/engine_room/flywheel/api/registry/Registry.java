package dev.engine_room.flywheel.api.registry;

import java.util.Set;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;

@ApiStatus.NonExtendable
public interface Registry<T> extends Iterable<T> {
	void register(T object);

	<S extends T> S registerAndGet(S object);

	@UnmodifiableView
	Set<T> getAll();

	boolean isFrozen();
}
