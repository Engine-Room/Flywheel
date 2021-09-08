package com.jozufozu.flywheel.light;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.LongConsumer;

import com.jozufozu.flywheel.util.WeakHashSet;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongRBTreeSet;
import it.unimi.dsi.fastutil.longs.LongSet;

public class WeakContainmentMultiMap<T> extends AbstractCollection<T> {

	private final Long2ObjectMap<WeakHashSet<T>> forward;
	private final WeakHashMap<T, LongSet> reverse;

	public WeakContainmentMultiMap() {
		forward = new Long2ObjectOpenHashMap<>();
		reverse = new WeakHashMap<>();
	}

	/**
	 * This is a confusing function, but it maintains the internal state of the chunk/section maps.
	 *
	 * <p>
	 *     First, uses the reverse lookup map to remove listener from all sets in the lookup map.<br>
	 *     Then, clears the listeners containment set.
	 * </p>
	 *
	 * @param listener The listener to clean up.
	 * @return An empty set that should be populated with the chunks/sections the listener is contained in.
	 */
	public LongSet getAndResetContainment(T listener) {
		LongSet containmentSet = reverse.computeIfAbsent(listener, $ -> new LongRBTreeSet());

		containmentSet.forEach((LongConsumer) l -> {
			WeakHashSet<T> listeners = forward.get(l);

			if (listeners != null) listeners.remove(listener);
		});

		containmentSet.clear();

		return containmentSet;
	}

	public Set<T> get(long l) {
		return forward.get(l);
	}

	public void put(long sectionPos, T listener) {
		forward.computeIfAbsent(sectionPos, $ -> new WeakHashSet<>()).add(listener);
	}

	@Override
	public boolean remove(Object o) {
		LongSet containmentSet = reverse.remove(o);

		if (containmentSet != null) {
			containmentSet.forEach((LongConsumer) l -> {
				WeakHashSet<T> listeners = forward.get(l);

				if (listeners != null) listeners.remove(o);
			});

			containmentSet.clear();

			return true;
		}
		return false;
	}

	@Override
	public Iterator<T> iterator() {
		return reverse.keySet().iterator();
	}

	@Override
	public int size() {
		return reverse.size();
	}
}
