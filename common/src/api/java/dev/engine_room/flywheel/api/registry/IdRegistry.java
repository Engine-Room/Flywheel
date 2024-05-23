package dev.engine_room.flywheel.api.registry;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import net.minecraft.resources.ResourceLocation;

@ApiStatus.NonExtendable
public interface IdRegistry<T> extends Iterable<T>  {
	void register(ResourceLocation id, T object);

	<S extends T> S registerAndGet(ResourceLocation id, S object);

	@Nullable
	T get(ResourceLocation id);

	@Nullable
	ResourceLocation getId(T object);

	T getOrThrow(ResourceLocation id);

	ResourceLocation getIdOrThrow(T object);

	@Unmodifiable
	Set<ResourceLocation> getAllIds();

	@Unmodifiable
	Collection<T> getAll();

	void addFreezeCallback(Consumer<IdRegistry<T>> callback);

	boolean isFrozen();
}
