package com.jozufozu.flywheel.lib.light;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.lib.box.Box;
import com.jozufozu.flywheel.lib.util.FlwUtil;
import com.jozufozu.flywheel.lib.util.LevelAttached;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.event.TickEvent;

/**
 * Keeps track of what chunks/sections each listener is in, so we can update exactly what needs to be updated.
 *
 * @apiNote Custom/fake levels (that are {@code != Minecraft.getInstance.level}) need to implement
 *          {@link LightUpdatedLevel} for LightUpdater to work with them.
 */
public class LightUpdater {
	private static final LevelAttached<LightUpdater> UPDATERS = new LevelAttached<>(level -> new LightUpdater());

	private final WeakContainmentMultiMap<LightListener> listenersBySection = new WeakContainmentMultiMap<>();
	private final Set<TickingLightListener> tickingListeners = FlwUtil.createWeakHashSet();

	private final Queue<LightListener> additionQueue = new ConcurrentLinkedQueue<>();

	public static boolean supports(LevelAccessor level) {
		// The client level is guaranteed to receive updates.
		if (Minecraft.getInstance().level == level) {
			return true;
		}
		// Custom/fake levels need to indicate that LightUpdater has meaning.
		if (level instanceof LightUpdatedLevel c) {
			return c.receivesLightUpdates();
		}
		return false;
	}

	public static LightUpdater get(LevelAccessor level) {
		if (supports(level)) {
			// The level is valid, so add it to the map.
			return UPDATERS.get(level);
		} else {
			// Fake light updater for a fake level.
			return DummyLightUpdater.INSTANCE;
		}
	}

	/**
	 * Add a listener.
	 *
	 * @param listener The object that wants to receive light update notifications.
	 */
	public void addListener(LightListener listener) {
		additionQueue.add(listener);
	}

	public void removeListener(LightListener listener) {
		listenersBySection.remove(listener);
	}

	/**
	 * Dispatch light updates to all registered {@link LightListener}s.
	 *
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

	public Stream<Box> getAllBoxes() {
		return listenersBySection.stream().map(LightListener::getVolume);
	}

	public boolean isEmpty() {
		return listenersBySection.isEmpty();
	}

	@ApiStatus.Internal
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END && FlwUtil.isGameActive()) {
			get(Minecraft.getInstance().level)
					.tick();
		}
	}

	void tick() {
		processQueue();

		for (TickingLightListener tickingListener : tickingListeners) {
			if (tickingListener.tickLightListener()) {
				addListener(tickingListener);
			}
		}
	}

	private synchronized void processQueue() {
		LightListener listener;
		while ((listener = additionQueue.poll()) != null) {
			doAdd(listener);
		}
	}

	private void doAdd(LightListener listener) {
		if (listener instanceof TickingLightListener ticking) {
			tickingListeners.add(ticking);
		}

		Box box = listener.getVolume();

		LongSet sections = listenersBySection.getAndResetContainment(listener);

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
					listenersBySection.put(sectionPos, listener);
					sections.add(sectionPos);
				}
			}
		}
	}
}
