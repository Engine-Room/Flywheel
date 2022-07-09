package com.jozufozu.flywheel.light;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.ParallelTaskEngine;
import com.jozufozu.flywheel.util.WeakHashSet;
import com.jozufozu.flywheel.util.WorldAttached;
import com.jozufozu.flywheel.util.box.GridAlignedBB;
import com.jozufozu.flywheel.util.box.ImmutableBox;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
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
	private final ParallelTaskEngine taskEngine;

	public static LightUpdater get(LevelAccessor level) {
		if (LightUpdated.receivesLightUpdates(level)) {
			// The level is valid, add it to the map.
			return LEVELS.get(level);
		} else {
			// Fake light updater for a fake level.
			return DummyLightUpdater.INSTANCE;
		}
	}

	private final LevelAccessor level;

	private final WeakHashSet<TickingLightListener> tickingLightListeners = new WeakHashSet<>();
	private final WeakContainmentMultiMap<LightListener> sections = new WeakContainmentMultiMap<>();
	private final WeakContainmentMultiMap<LightListener> chunks = new WeakContainmentMultiMap<>();

	public LightUpdater(LevelAccessor level) {
		taskEngine = Backend.getTaskEngine();
		this.level = level;
	}

	public void tick() {
		tickSerial();
		//tickParallel();
	}

	private void tickSerial() {
		for (TickingLightListener tickingLightListener : tickingLightListeners) {
			if (tickingLightListener.tickLightListener()) {
				addListener(tickingLightListener);
			}
		}
	}

	private void tickParallel() {
		Queue<LightListener> listeners = new ConcurrentLinkedQueue<>();

		taskEngine.group("LightUpdater")
				.addTasks(tickingLightListeners.stream(), listener -> {
					if (listener.tickLightListener()) {
						listeners.add(listener);
					}
				})
				.onComplete(() -> listeners.forEach(this::addListener))
				.submit();
	}

	/**
	 * Add a listener.
	 *
	 * @param listener The object that wants to receive light update notifications.
	 */
	public void addListener(LightListener listener) {
		if (listener instanceof TickingLightListener)
			tickingLightListeners.add(((TickingLightListener) listener));

		ImmutableBox box = listener.getVolume();

		LongSet sections = this.sections.getAndResetContainment(listener);
		LongSet chunks = this.chunks.getAndResetContainment(listener);

		int minX = SectionPos.blockToSectionCoord(box.getMinX());
		int minY = SectionPos.blockToSectionCoord(box.getMinY());
		int minZ = SectionPos.blockToSectionCoord(box.getMinZ());
		int maxX = SectionPos.blockToSectionCoord(box.getMaxX());
		int maxY = SectionPos.blockToSectionCoord(box.getMaxY());
		int maxZ = SectionPos.blockToSectionCoord(box.getMaxZ());

		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				for (int y = minY; y <= maxY; y++) {
					long sectionPos = SectionPos.asLong(x, y, z);
					this.sections.put(sectionPos, listener);
					sections.add(sectionPos);
				}
				long chunkPos = SectionPos.asLong(x, 0, z);
				this.chunks.put(chunkPos, listener);
				chunks.add(chunkPos);
			}
		}
	}

	public void removeListener(LightListener listener) {
		this.sections.remove(listener);
		this.chunks.remove(listener);
	}

	/**
	 * Dispatch light updates to all registered {@link LightListener}s.
	 * @param type       The type of light that changed.
	 * @param sectionPos A long representing the section position where light changed.
	 */
	public void onLightUpdate(LightLayer type, long sectionPos) {
		Set<LightListener> set = sections.get(sectionPos);

		if (set == null || set.isEmpty()) return;

		set.removeIf(LightListener::isListenerInvalid);

		ImmutableBox chunkBox = GridAlignedBB.from(SectionPos.of(sectionPos));

		for (LightListener listener : set) {
			listener.onLightUpdate(type, chunkBox);
		}
	}

	/**
	 * Dispatch light updates to all registered {@link LightListener}s
	 * when the server sends lighting data for an entire chunk.
	 *
	 */
	public void onLightPacket(int chunkX, int chunkZ) {
		long chunkPos = SectionPos.asLong(chunkX, 0, chunkZ);

		Set<LightListener> set = chunks.get(chunkPos);

		if (set == null || set.isEmpty()) return;

		set.removeIf(LightListener::isListenerInvalid);

		for (LightListener listener : set) {
			listener.onLightPacket(chunkX, chunkZ);
		}
	}

	public static long blockToSection(BlockPos pos) {
		return SectionPos.asLong(pos.getX(), pos.getY(), pos.getZ());
	}

	public static long sectionToChunk(long sectionPos) {
		return sectionPos & 0xFFFFFFFFFFF_00000L;
	}

	public Stream<ImmutableBox> getAllBoxes() {
		return chunks.stream().map(LightListener::getVolume);
	}

	public boolean isEmpty() {
		return chunks.isEmpty();
	}
}
