package com.jozufozu.flywheel.impl;

import java.util.Iterator;
import java.util.Set;

import org.jetbrains.annotations.Unmodifiable;

import com.jozufozu.flywheel.api.registry.Registry;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;

public class RegistryImpl<T> implements Registry<T> {
	private static final ObjectList<RegistryImpl<?>> ALL = new ObjectArrayList<>();

	private final ObjectSet<T> set = new ObjectOpenHashSet<>();
	private final ObjectSet<T> setView = ObjectSets.unmodifiable(set);
	private final ObjectList<Runnable> freezeCallbacks = new ObjectArrayList<>();
	private boolean frozen;

	private RegistryImpl() {
		ALL.add(this);
	}

	public static <T> Registry<T> create() {
		return new RegistryImpl<>();
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
	@Unmodifiable
	public Set<T> getAll() {
		return setView;
	}

	@Override
	public void addFreezeCallback(Runnable callback) {
		if (frozen) {
			throw new IllegalStateException("Cannot add freeze callback to frozen registry!");
		}
		freezeCallbacks.add(callback);
	}

	@Override
	public boolean isFrozen() {
		return frozen;
	}

	@Override
	public Iterator<T> iterator() {
		return getAll().iterator();
	}

	public void freeze() {
		frozen = true;
		for (Runnable runnable : freezeCallbacks) {
			runnable.run();
		}
		freezeCallbacks.clear();
	}

	public static void freezeAll() {
		for (RegistryImpl<?> registry : ALL) {
			registry.freeze();
		}
	}
}
