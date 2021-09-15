package com.jozufozu.flywheel.light;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.jozufozu.flywheel.util.WeakHashSet;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;

/**
 * Keeps track of what chunks/sections each listener is in, so we can update exactly what needs to be updated.
 */
public class LightUpdater {

	private static final Map<BlockAndTintGetter, LightUpdater> light = new HashMap<>();
	public static LightUpdater get(BlockAndTintGetter world) {
		return light.computeIfAbsent(world, LightUpdater::new);
	}

	private final LightProvider provider;

	private final WeakHashSet<IMovingListener> movingListeners = new WeakHashSet<>();
	private final WeakContainmentMultiMap<ILightUpdateListener> sections = new WeakContainmentMultiMap<>();
	private final WeakContainmentMultiMap<ILightUpdateListener> chunks = new WeakContainmentMultiMap<>();

	public LightUpdater(BlockAndTintGetter world) {
		provider = BasicProvider.get(world);
	}

	public LightProvider getProvider() {
		return provider;
	}

	public void tick() {
		for (IMovingListener listener : movingListeners) {
			if (listener.update(provider)) {
				addListener(listener);
			}
		}
	}

	/**
	 * Add a listener.

	 * @param listener The object that wants to receive light update notifications.
	 */
	public void addListener(ILightUpdateListener listener) {
		if (listener instanceof IMovingListener)
			movingListeners.add(((IMovingListener) listener));

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

	public void removeListener(ILightUpdateListener listener) {
		this.sections.remove(listener);
		this.chunks.remove(listener);
	}

	/**
	 * Dispatch light updates to all registered {@link ILightUpdateListener}s.
	 * @param type       The type of light that changed.
	 * @param sectionPos A long representing the section position where light changed.
	 */
	public void onLightUpdate(LightLayer type, long sectionPos) {
		Set<ILightUpdateListener> set = sections.get(sectionPos);

		if (set == null || set.isEmpty()) return;

		set.removeIf(l -> l.status().shouldRemove());

		ImmutableBox chunkBox = GridAlignedBB.from(SectionPos.of(sectionPos));

		for (ILightUpdateListener listener : set) {
			listener.onLightUpdate(provider, type, chunkBox);
		}
	}

	/**
	 * Dispatch light updates to all registered {@link ILightUpdateListener}s
	 * when the server sends lighting data for an entire chunk.
	 *
	 */
	public void onLightPacket(int chunkX, int chunkZ) {
		long chunkPos = SectionPos.asLong(chunkX, 0, chunkZ);

		Set<ILightUpdateListener> set = chunks.get(chunkPos);

		if (set == null || set.isEmpty()) return;

		set.removeIf(l -> l.status().shouldRemove());

		for (ILightUpdateListener listener : set) {
			listener.onLightPacket(provider, chunkX, chunkZ);
		}
	}

	public static long blockToSection(BlockPos pos) {
		return SectionPos.asLong(pos.getX(), pos.getY(), pos.getZ());
	}

	public static long sectionToChunk(long sectionPos) {
		return sectionPos & 0xFFFFFFFFFFF_00000L;
	}

	public Stream<ImmutableBox> getAllBoxes() {
		return chunks.stream().map(ILightUpdateListener::getVolume);
	}

	public boolean isEmpty() {
		return chunks.isEmpty();
	}
}
