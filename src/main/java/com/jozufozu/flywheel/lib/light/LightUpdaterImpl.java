package com.jozufozu.flywheel.lib.light;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.api.visualization.LightUpdater;
import com.jozufozu.flywheel.lib.box.Box;
import com.jozufozu.flywheel.lib.task.PlanUtil;
import com.jozufozu.flywheel.lib.task.SimplyComposedPlan;
import com.jozufozu.flywheel.lib.task.Synchronizer;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;

/**
 * Keeps track of what chunks/sections each listener is in, so we can update exactly what needs to be updated.
 */
public class LightUpdaterImpl implements LightUpdater {
	private final Map<LightListener, LongSet> listenersAndTheirSections = new WeakHashMap<>();
	private final Long2ObjectMap<List<LightListener>> listenersBySection = new Long2ObjectOpenHashMap<>();

	private final Queue<LightListener> additionQueue = new ConcurrentLinkedQueue<>();
	private final LongSet sectionsQueue = new LongOpenHashSet();

	/**
	 * Add a listener.
	 *
	 * @param listener The object that wants to receive light update notifications.
	 */
	public void addListener(LightListener listener) {
		additionQueue.add(listener);
	}

	public void removeListener(LightListener listener) {
		listenersAndTheirSections.remove(listener);
	}

	public Plan<RenderContext> plan() {
		return (SimplyComposedPlan<RenderContext>) (TaskExecutor taskExecutor, RenderContext context, Runnable onCompletion) -> {
			processQueue();

			if (sectionsQueue.isEmpty()) {
				onCompletion.run();
				return;
			}

			var sync = new Synchronizer(sectionsQueue.size(), () -> {
				sectionsQueue.clear();
				onCompletion.run();
			});

			sectionsQueue.forEach((long section) -> {
				List<LightListener> listeners = listenersBySection.get(section);
				if (listeners != null && !listeners.isEmpty()) {
					taskExecutor.execute(() -> {
						PlanUtil.distribute(taskExecutor, SectionPos.of(section), sync, listeners, (listener, pos) -> {
							listener.onLightUpdate(LightLayer.BLOCK, pos);
						});
					});
				} else {
					sync.decrementAndEventuallyRun();
				}
			});
		};
	}

	public Stream<Box> getAllBoxes() {
		return listenersAndTheirSections.keySet()
				.stream()
				.map(LightListener::getVolume);
	}

	public boolean isEmpty() {
		return listenersAndTheirSections.isEmpty();
	}

	private synchronized void processQueue() {
		LightListener listener;
		while ((listener = additionQueue.poll()) != null) {
			doAdd(listener);
		}
	}

	private void doAdd(LightListener listener) {
		Box box = listener.getVolume();

		LongSet sections = new LongArraySet();

		int minX = SectionPos.blockToSectionCoord(box.getMinX());
		int minY = SectionPos.blockToSectionCoord(box.getMinY());
		int minZ = SectionPos.blockToSectionCoord(box.getMinZ());
		int maxX = SectionPos.blockToSectionCoord(box.getMaxX());
		int maxY = SectionPos.blockToSectionCoord(box.getMaxY());
		int maxZ = SectionPos.blockToSectionCoord(box.getMaxZ());

		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					var longPos = SectionPos.asLong(x, y, z);
					sections.add(longPos);
					listenersBySection.computeIfAbsent(longPos, $ -> new ArrayList<>())
							.add(listener);
				}
			}
		}

		listenersAndTheirSections.put(listener, sections);
	}

	public void notifySectionUpdates(LongSet sections) {
		sectionsQueue.addAll(sections);
	}
}
