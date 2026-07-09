package dev.engine_room.flywheel.api.registry;

import java.util.Collection;
import java.util.Set;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import net.minecraft.resources.Identifier;

@ApiStatus.NonExtendable
public interface IdRegistry<T> extends Iterable<T>  {
	void register(Identifier id, T object);

	<S extends T> S registerAndGet(Identifier id, S object);

	@Nullable
	T get(Identifier id);

	@Nullable
	Identifier getId(T object);

	T getOrThrow(Identifier id);

	Identifier getIdOrThrow(T object);

	@UnmodifiableView
	Set<Identifier> getAllIds();

	@UnmodifiableView
	Collection<T> getAll();

	boolean isFrozen();
}
