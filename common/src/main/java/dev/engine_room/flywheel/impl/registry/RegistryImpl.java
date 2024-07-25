package dev.engine_room.flywheel.impl.registry;

import java.util.Iterator;
import java.util.Set;

import org.jetbrains.annotations.UnmodifiableView;

import dev.engine_room.flywheel.api.registry.Registry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;

public class RegistryImpl<T> implements Registry<T> {
	private static final ObjectList<RegistryImpl<?>> ALL = new ObjectArrayList<>();

	private final ObjectSet<T> set = ObjectSets.synchronize(new ObjectOpenHashSet<>());
	private final ObjectSet<T> setView = ObjectSets.unmodifiable(set);
	private boolean frozen;

	public RegistryImpl() {
		ALL.add(this);
	}

	@Override
	public void register(T object) {
		if (frozen) {
			throw new IllegalStateException("Cannot register to frozen registry!");
		}
		boolean added = set.add(object);
		if (!added) {
			throw new IllegalArgumentException("Cannot override registration!");
		}
	}

	@Override
	public <S extends T> S registerAndGet(S object) {
		register(object);
		return object;
	}

	@Override
	@UnmodifiableView
	public Set<T> getAll() {
		return setView;
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
		for (RegistryImpl<?> registry : ALL) {
			registry.freeze();
		}
	}
}
