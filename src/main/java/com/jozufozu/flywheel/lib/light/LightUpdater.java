package com.jozufozu.flywheel.lib.light;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import com.jozufozu.flywheel.lib.box.ImmutableBox;
import com.jozufozu.flywheel.util.FlwUtil;
import com.jozufozu.flywheel.util.WorldAttached;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;

/**
 * Keeps track of what chunks/sections each listener is in, so we can update exactly what needs to be updated.
 *
 * @apiNote Custom/fake levels (that are {@code != Minecraft.getInstance.level}) need to implement
 *          {@link LightUpdated} for LightUpdater to work with them.
 */
public class LightUpdater {
	private static final WorldAttached<LightUpdater> LEVELS = new WorldAttached<>(LightUpdater::new);

	private final LevelAccessor level;

	private final WeakContainmentMultiMap<LightListener> listenersBySection = new WeakContainmentMultiMap<>();
	private final Set<TickingLightListener> tickingListeners = FlwUtil.createWeakHashSet();

	private final Queue<LightListener> queue = new ConcurrentLinkedQueue<>();

	public static LightUpdater get(LevelAccessor level) {
		if (LightUpdated.receivesLightUpdates(level)) {
			// The level is valid, add it to the map.
			return LEVELS.get(level);
		} else {
			// Fake light updater for a fake level.
			return DummyLightUpdater.INSTANCE;
		}
	}

	public LightUpdater(LevelAccessor level) {
		this.level = level;
	}

	public void tick() {
		processQueue();
		tickSerial();
	}

	private void tickSerial() {
		for (TickingLightListener tickingLightListener : tickingListeners) {
			if (tickingLightListener.tickLightListener()) {
				addListener(tickingLightListener);
			}
		}
	}

	/**
	 * Add a listener.
	 *
	 * @param listener The object that wants to receive light update notifications.
	 */
	public void addListener(LightListener listener) {
		queue.add(listener);
	}

	private synchronized void processQueue() {
		LightListener listener;
		while ((listener = queue.poll()) != null) {
			doAdd(listener);
		}
	}

	private void doAdd(LightListener listener) {
		if (listener instanceof TickingLightListener) {
			tickingListeners.add(((TickingLightListener) listener));
		}

		ImmutableBox box = listener.getVolume();

		LongSet sections = this.listenersBySection.getAndResetContainment(listener);

		int minX = SectionPos.blockToSectionCoord(box.getMinX());
		int minY = SectionPos.blockToSectionCoord(box.getMinY());
		int minZ = SectionPos.blockToSectionCoord(box.getMinZ());
		int maxX = SectionPos.blockToSectionCoord(box.getMaxX());
		int maxY = SectionPos.blockToSectionCoord(box.getMaxY());
		int maxZ = SectionPos.blockToSectionCoord(box.getMaxZ());

		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					long sectionPos = SectionPos.asLong(x, y, z);
					this.listenersBySection.put(sectionPos, listener);
					sections.add(sectionPos);
				}
			}
		}
	}

	public void removeListener(LightListener listener) {
		this.listenersBySection.remove(listener);
	}

	/**
	 * Dispatch light updates to all registered {@link LightListener}s.
	 * @param type The type of light that changed.
	 * @param pos  The section position where light changed.
	 */
	public void onLightUpdate(LightLayer type, SectionPos pos) {
		processQueue();

		Set<LightListener> listeners = listenersBySection.get(pos.asLong());

		if (listeners == null || listeners.isEmpty()) {
			return;
		}

		listeners.removeIf(LightListener::isInvalid);

		for (LightListener listener : listeners) {
			listener.onLightUpdate(type, pos);
		}
	}

	public Stream<ImmutableBox> getAllBoxes() {
		return listenersBySection.stream().map(LightListener::getVolume);
	}

	public boolean isEmpty() {
		return listenersBySection.isEmpty();
	}
}
