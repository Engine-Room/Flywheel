package com.jozufozu.flywheel.light;

import java.util.Set;

import com.jozufozu.flywheel.util.WeakHashSet;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.LightType;

public class LightUpdater {

	private static LightUpdater instance;

	public static LightUpdater getInstance() {
		if (instance == null) instance = new LightUpdater();

		return instance;
	}

	private final WeakHashSet<ILightUpdateListener> allListeners;
	private final WeakContainmentMultiMap<ILightUpdateListener> sections;
	private final WeakContainmentMultiMap<ILightUpdateListener> chunks;

	public LightUpdater() {
		allListeners = new WeakHashSet<>();
		sections = new WeakContainmentMultiMap<>();
		chunks = new WeakContainmentMultiMap<>();
	}

	public void tick() {
		for (ILightUpdateListener listener : allListeners) {
			if (listener.status() == ListenerStatus.UPDATE) {
				addListener(listener);

				listener.onLightUpdate(Minecraft.getInstance().level, LightType.BLOCK, null);
			}
		}
	}

	/**
	 * Add a listener.

	 * @param listener The object that wants to receive light update notifications.
	 */
	public void addListener(ILightUpdateListener listener) {
		allListeners.add(listener);

		Volume volume = listener.getVolume();

		LongSet sections = this.sections.getAndResetContainment(listener);
		LongSet chunks = this.chunks.getAndResetContainment(listener);

		if (volume instanceof Volume.Block) {
			BlockPos pos = ((Volume.Block) volume).pos;
			long sectionPos = blockToSection(pos);
			this.sections.put(sectionPos, listener);
			sections.add(sectionPos);

			long chunkPos = sectionToChunk(sectionPos);
			this.chunks.put(chunkPos, listener);
			chunks.add(chunkPos);
		} else if (volume instanceof Volume.Box) {
			GridAlignedBB box = ((Volume.Box) volume).box;

			int minX = SectionPos.blockToSectionCoord(box.minX);
			int minY = SectionPos.blockToSectionCoord(box.minY);
			int minZ = SectionPos.blockToSectionCoord(box.minZ);
			int maxX = SectionPos.blockToSectionCoord(box.maxX);
			int maxY = SectionPos.blockToSectionCoord(box.maxY);
			int maxZ = SectionPos.blockToSectionCoord(box.maxZ);

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
	}

	/**
	 * Dispatch light updates to all registered {@link ILightUpdateListener}s.
	 *
	 * @param world      The world in which light was updated.
	 * @param type       The type of light that changed.
	 * @param sectionPos A long representing the section position where light changed.
	 */
	public void onLightUpdate(IBlockDisplayReader world, LightType type, long sectionPos) {
		Set<ILightUpdateListener> set = sections.get(sectionPos);

		if (set == null || set.isEmpty()) return;

		set.removeIf(l -> l.status().shouldRemove());

		GridAlignedBB chunkBox = GridAlignedBB.from(SectionPos.of(sectionPos));

		for (ILightUpdateListener listener : set) {
			listener.onLightUpdate(world, type, chunkBox.copy());
		}
	}

	/**
	 * Dispatch light updates to all registered {@link ILightUpdateListener}s
	 * when the server sends lighting data for an entire chunk.
	 *
	 * @param world The world in which light was updated.
	 */
	public void onLightPacket(IBlockDisplayReader world, int chunkX, int chunkZ) {
		long chunkPos = SectionPos.asLong(chunkX, 0, chunkZ);

		Set<ILightUpdateListener> set = chunks.get(chunkPos);

		if (set == null || set.isEmpty()) return;

		set.removeIf(l -> l.status().shouldRemove());

		for (ILightUpdateListener listener : set) {
			listener.onLightPacket(world, chunkX, chunkZ);
		}
	}

	public static long blockToSection(BlockPos pos) {
		return SectionPos.asLong(pos.getX(), pos.getY(), pos.getZ());
	}

	public static long sectionToChunk(long sectionPos) {
		return sectionPos & 0xFFFFFFFFFFF_00000L;
	}
}
