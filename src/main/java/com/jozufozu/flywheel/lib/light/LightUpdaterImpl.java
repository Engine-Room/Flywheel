package com.jozufozu.flywheel.lib.light;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.visualization.LightUpdater;
import com.jozufozu.flywheel.lib.box.Box;
import com.jozufozu.flywheel.lib.task.ForEachPlan;
import com.jozufozu.flywheel.lib.task.IfElsePlan;
import com.jozufozu.flywheel.lib.task.SimplePlan;
import com.jozufozu.flywheel.lib.util.FlwUtil;

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;

/**
 * Keeps track of what chunks/sections each listener is in, so we can update exactly what needs to be updated.
 *
 * @apiNote Custom/fake levels (that are {@code != Minecraft.getInstance.level}) need to implement
 *          {@link LightUpdatedLevel} for LightUpdater to work with them.
 */
public class LightUpdaterImpl implements LightUpdater {
	private final WeakHashMap<LightListener, LongSet> listenersAndTheirSections = new WeakHashMap<>();
	private final Set<TickingLightListener> tickingListeners = FlwUtil.createWeakHashSet();

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
		// Assume we'll have more listeners than sections updated
		// TODO: this is slow, maybe launch a task for each changed section and distribute from there?
		return SimplePlan.<RenderContext>of(this::processQueue)
				.then(IfElsePlan.<RenderContext>on(() -> !sectionsQueue.isEmpty())
						.ifTrue(ForEachPlan.of(() -> listenersAndTheirSections.entrySet()
								.stream()
								.toList(), this::updateOneEntry))
						.plan())
				.then(SimplePlan.of(() -> sectionsQueue.clear()));
	}

	private void updateOneEntry(Map.Entry<LightListener, LongSet> entry) {

		updateOne(entry.getKey(), entry.getValue());

	}

	private void updateOne(LightListener listener, LongSet containedSections) {
		for (long l : containedSections.toLongArray()) {
			if (sectionsQueue.contains(l)) {
				listener.onLightUpdate(LightLayer.BLOCK, SectionPos.of(l));
				break;
			}
		}
	}

	public Stream<Box> getAllBoxes() {
		return listenersAndTheirSections.keySet()
				.stream()
				.map(LightListener::getVolume);
	}

	public boolean isEmpty() {
		return listenersAndTheirSections.isEmpty();
	}

	public void tick() {
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
					sections.add(SectionPos.asLong(x, y, z));
				}
			}
		}

		listenersAndTheirSections.put(listener, sections);
	}

	public void notifySectionUpdates(LongSet sections) {
		sectionsQueue.addAll(sections);
	}
}
