package dev.engine_room.flywheel.impl.registry;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import dev.engine_room.flywheel.api.registry.IdRegistry;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceCollection;
import it.unimi.dsi.fastutil.objects.ReferenceCollections;
import net.minecraft.resources.Identifier;

public class IdRegistryImpl<T> implements IdRegistry<T> {
	private static final ObjectList<IdRegistryImpl<?>> ALL = new ObjectArrayList<>();

	private final Object2ReferenceMap<Identifier, T> map = Object2ReferenceMaps.synchronize(new Object2ReferenceOpenHashMap<>());
	private final Reference2ObjectMap<T, Identifier> reverseMap = Reference2ObjectMaps.synchronize(new Reference2ObjectOpenHashMap<>());
	private final ObjectSet<Identifier> keysView = ObjectSets.unmodifiable(map.keySet());
	private final ReferenceCollection<T> valuesView = ReferenceCollections.unmodifiable(map.values());
	private boolean frozen;

	public IdRegistryImpl() {
		ALL.add(this);
	}

	@Override
	public void register(Identifier id, T object) {
		if (frozen) {
			throw new IllegalStateException("Cannot register to frozen registry!");
		}
		T oldValue = map.put(id, object);
		if (oldValue != null) {
			throw new IllegalArgumentException("Cannot override registration for ID '" + id + "'!");
		}
		Identifier oldId = reverseMap.put(object, id);
		if (oldId != null) {
			throw new IllegalArgumentException("Cannot override ID '" + id + "' with registration for ID '" + oldId + "'!");
		}
	}

	@Override
	public <S extends T> S registerAndGet(Identifier id, S object) {
		register(id, object);
		return object;
	}

	@Override
	@Nullable
	public T get(Identifier id) {
		return map.get(id);
	}

	@Override
	@Nullable
	public Identifier getId(T object) {
		return reverseMap.get(object);
	}

	@Override
	public T getOrThrow(Identifier id) {
		T object = get(id);
		if (object == null) {
			throw new IllegalArgumentException("Could not find object for ID '" + id + "'!");
		}
		return object;
	}

	@Override
	public Identifier getIdOrThrow(T object) {
		Identifier id = getId(object);
		if (id == null) {
			throw new IllegalArgumentException("Could not find ID for object!");
		}
		return id;
	}

	@Override
	@UnmodifiableView
	public Set<Identifier> getAllIds() {
		return keysView;
	}

	@Override
	@UnmodifiableView
	public Collection<T> getAll() {
		return valuesView;
	}

	@Override
	public boolean isFrozen() {
		return frozen;
	}

	@Override
	public Iterator<T> iterator() {
		return getAll().iterator();
	}

	private void freeze() {
		frozen = true;
	}

	public static void freezeAll() {
		for (IdRegistryImpl<?> registry : ALL) {
			registry.freeze();
		}
	}
}
