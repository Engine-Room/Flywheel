package com.jozufozu.flywheel.lib.util;

import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.level.LevelEvent;

public final class LevelAttached<T> {
	private static final ConcurrentLinkedDeque<WeakReference<LevelAttached<?>>> ALL = new ConcurrentLinkedDeque<>();
	private static final Cleaner CLEANER = Cleaner.create();

	private final LoadingCache<LevelAccessor, T> cache;

	public LevelAttached(Function<LevelAccessor, T> factory, Consumer<T> finalizer) {
		WeakReference<LevelAttached<?>> thisRef = new WeakReference<>(this);
		ALL.add(thisRef);

		cache = CacheBuilder.newBuilder()
				.<LevelAccessor, T>removalListener(n -> finalizer.accept(n.getValue()))
				.build(new CacheLoader<>() {
					@Override
					public T load(LevelAccessor key) {
						return factory.apply(key);
					}
				});

		CLEANER.register(this, new CleaningAction(thisRef, cache));
	}

	public LevelAttached(Function<LevelAccessor, T> factory) {
		this(factory, t -> {});
	}

	@ApiStatus.Internal
	public static void onUnloadLevel(LevelEvent.Unload event) {
		invalidateLevel(event.getLevel());
	}

	public static void invalidateLevel(LevelAccessor level) {
		Iterator<WeakReference<LevelAttached<?>>> iterator = ALL.iterator();
		while (iterator.hasNext()) {
			LevelAttached<?> attached = iterator.next().get();
			if (attached == null) {
				iterator.remove();
			} else {
				attached.remove(level);
			}
		}
	}

	public T get(LevelAccessor level) {
		return cache.getUnchecked(level);
	}

	public void remove(LevelAccessor level) {
		cache.invalidate(level);
	}

	public T refresh(LevelAccessor level) {
		remove(level);
		return get(level);
	}

	public void reset() {
		cache.invalidateAll();
	}

	private static class CleaningAction implements Runnable {
		private final WeakReference<LevelAttached<?>> ref;
		private final LoadingCache<LevelAccessor, ?> cache;

		private CleaningAction(WeakReference<LevelAttached<?>> ref, LoadingCache<LevelAccessor, ?> cache) {
			this.ref = ref;
			this.cache = cache;
		}

		@Override
		public void run() {
			ALL.remove(ref);
			cache.invalidateAll();
		}
	}
}
