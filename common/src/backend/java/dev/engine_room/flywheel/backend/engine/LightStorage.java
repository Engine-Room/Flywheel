package dev.engine_room.flywheel.backend.engine;

import java.util.BitSet;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.api.event.RenderContext;
import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.backend.LightUpdateHolder;
import dev.engine_room.flywheel.backend.engine.indirect.StagingBuffer;
import dev.engine_room.flywheel.backend.gl.buffer.GlBuffer;
import dev.engine_room.flywheel.lib.task.SimplePlan;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;

/**
 * TODO: AO data
 * A managed arena of light sections for uploading to the GPU.
 *
 * <p>Each section represents an 18x18x18 block volume of light data.
 * The "edges" are taken from the neighboring sections, so that each
 * shader invocation only needs to access a single section of data.
 * Even still, neighboring shader invocations may need to access other sections.
 *
 * <p>Sections are logically stored as a 9x9x9 array of longs,
 * where each long holds a 2x2x2 array of light data.
 * <br>Both the greater array and the longs are packed in x, z, y order.
 *
 * <p>Thus, each section occupies 5832 bytes.
 */
public class LightStorage {
	public static final long SECTION_SIZE_BYTES = 9 * 9 * 9 * 8;
	private static final int DEFAULT_ARENA_CAPACITY_SECTIONS = 64;
	private static final int INVALID_SECTION = -1;

	private final LevelAccessor level;

	private final Arena arena;
	private final Long2IntMap section2ArenaIndex = new Long2IntOpenHashMap();
	{
		section2ArenaIndex.defaultReturnValue(INVALID_SECTION);
	}

	private final BitSet changed = new BitSet();
	private boolean needsLutRebuild = false;

	@Nullable
	private LongSet requestedSections;

	public LightStorage(LevelAccessor level) {
		this.level = level;

		arena = new Arena(SECTION_SIZE_BYTES, DEFAULT_ARENA_CAPACITY_SECTIONS);
	}

	/**
	 * Set the set of requested sections.
	 * <p> When set, this will be processed in the next frame plan. It may not be set every frame.
	 *
	 * @param sections The set of sections requested by the impl.
	 */
	public void sections(LongSet sections) {
		requestedSections = sections;
	}

	public Plan<RenderContext> createFramePlan() {
		return SimplePlan.of(() -> {
			var updatedSections = LightUpdateHolder.get(level)
					.getAndClearUpdatedSections();

			if (updatedSections.isEmpty() && requestedSections == null) {
				return;
			}

			removeUnusedSections();

			// Start building the set of sections we need to collect this frame.
			LongSet sectionsToCollect;
			if (requestedSections == null) {
				// If none were requested, then we need to collect all sections that received updates.
				sectionsToCollect = new LongArraySet();
			} else {
				// If we did receive a new set of requested sections, we only
				// need to collect the sections that weren't yet tracked.
				sectionsToCollect = requestedSections;
				sectionsToCollect.removeAll(section2ArenaIndex.keySet());
			}

			// updatedSections contains all sections than received light updates,
			// but we only care about its intersection with our tracked sections.
			for (long updatedSection : updatedSections) {
				// Since sections contain the border light of their neighbors, we need to collect the neighbors as well.
				for (int x = -1; x <= 1; x++) {
					for (int y = -1; y <= 1; y++) {
						for (int z = -1; z <= 1; z++) {
							long section = SectionPos.offset(updatedSection, x, y, z);
							if (section2ArenaIndex.containsKey(section)) {
								sectionsToCollect.add(section);
							}
						}
					}
				}
			}

			// Now actually do the collection.
			// TODO: Should this be done in parallel?
			sectionsToCollect.forEach(this::collectSection);

			requestedSections = null;
		});
	}

	private void removeUnusedSections() {
		if (requestedSections == null) {
			return;
		}

		var entries = section2ArenaIndex.long2IntEntrySet();
		var it = entries.iterator();
		while (it.hasNext()) {
			var entry = it.next();
			var section = entry.getLongKey();

			if (!this.requestedSections.contains(section)) {
				arena.free(entry.getIntValue());
				needsLutRebuild = true;
				it.remove();
			}
		}
	}

	public int capacity() {
		return arena.capacity();
	}

	public void collectSection(long section) {
		var lightEngine = level.getLightEngine();

		var blockLight = lightEngine.getLayerListener(LightLayer.BLOCK);
		var skyLight = lightEngine.getLayerListener(LightLayer.SKY);

		int index = indexForSection(section);

		changed.set(index);

		long ptr = arena.indexToPointer(index);

		// Zero it out first. This is basically free and makes it easier to handle missing sections later.
		MemoryUtil.memSet(ptr, 0, SECTION_SIZE_BYTES);

		collectCenter(blockLight, skyLight, ptr, section);

		for (SectionEdge i : SectionEdge.values()) {
			collectYZPlane(blockLight, skyLight, ptr, SectionPos.offset(section, i.sectionOffset, 0, 0), i);
			collectXZPlane(blockLight, skyLight, ptr, SectionPos.offset(section, 0, i.sectionOffset, 0), i);
			collectXYPlane(blockLight, skyLight, ptr, SectionPos.offset(section, 0, 0, i.sectionOffset), i);

			for (SectionEdge j : SectionEdge.values()) {
				collectXStrip(blockLight, skyLight, ptr, SectionPos.offset(section, 0, i.sectionOffset, j.sectionOffset), i, j);
				collectYStrip(blockLight, skyLight, ptr, SectionPos.offset(section, i.sectionOffset, 0, j.sectionOffset), i, j);
				collectZStrip(blockLight, skyLight, ptr, SectionPos.offset(section, i.sectionOffset, j.sectionOffset, 0), i, j);
			}
		}

		collectCorners(blockLight, skyLight, ptr, section);
	}

	private void collectXStrip(LayerLightEventListener blockLight, LayerLightEventListener skyLight, long ptr, long section, SectionEdge y, SectionEdge z) {
		var pos = SectionPos.of(section);
		var blockData = blockLight.getDataLayerData(pos);
		var skyData = skyLight.getDataLayerData(pos);
		if (blockData == null || skyData == null) {
			return;
		}
		for (int x = 0; x < 16; x++) {
			write(ptr, x, y.relative, z.relative, blockData.get(x, y.pos, z.pos), skyData.get(x, y.pos, z.pos));
		}
	}

	private void collectYStrip(LayerLightEventListener blockLight, LayerLightEventListener skyLight, long ptr, long section, SectionEdge x, SectionEdge z) {
		var pos = SectionPos.of(section);
		var blockData = blockLight.getDataLayerData(pos);
		var skyData = skyLight.getDataLayerData(pos);
		if (blockData == null || skyData == null) {
			return;
		}
		for (int y = 0; y < 16; y++) {
			write(ptr, x.relative, y, z.relative, blockData.get(x.pos, y, z.pos), skyData.get(x.pos, y, z.pos));
		}
	}

	private void collectZStrip(LayerLightEventListener blockLight, LayerLightEventListener skyLight, long ptr, long section, SectionEdge x, SectionEdge y) {
		var pos = SectionPos.of(section);
		var blockData = blockLight.getDataLayerData(pos);
		var skyData = skyLight.getDataLayerData(pos);
		if (blockData == null || skyData == null) {
			return;
		}
		for (int z = 0; z < 16; z++) {
			write(ptr, x.relative, y.relative, z, blockData.get(x.pos, y.pos, z), skyData.get(x.pos, y.pos, z));
		}
	}

	private void collectYZPlane(LayerLightEventListener blockLight, LayerLightEventListener skyLight, long ptr, long section, SectionEdge x) {
		var pos = SectionPos.of(section);
		var blockData = blockLight.getDataLayerData(pos);
		var skyData = skyLight.getDataLayerData(pos);
		if (blockData == null || skyData == null) {
			return;
		}
		for (int y = 0; y < 16; y++) {
			for (int z = 0; z < 16; z++) {
				write(ptr, x.relative, y, z, blockData.get(x.pos, y, z), skyData.get(x.pos, y, z));
			}
		}
	}

	private void collectXZPlane(LayerLightEventListener blockLight, LayerLightEventListener skyLight, long ptr, long section, SectionEdge y) {
		var pos = SectionPos.of(section);
		var blockData = blockLight.getDataLayerData(pos);
		var skyData = skyLight.getDataLayerData(pos);
		if (blockData == null || skyData == null) {
			return;
		}
		for (int z = 0; z < 16; z++) {
			for (int x = 0; x < 16; x++) {
				write(ptr, x, y.relative, z, blockData.get(x, y.pos, z), skyData.get(x, y.pos, z));
			}
		}
	}

	private void collectXYPlane(LayerLightEventListener blockLight, LayerLightEventListener skyLight, long ptr, long section, SectionEdge z) {
		var pos = SectionPos.of(section);
		var blockData = blockLight.getDataLayerData(pos);
		var skyData = skyLight.getDataLayerData(pos);
		if (blockData == null || skyData == null) {
			return;
		}
		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				write(ptr, x, y, z.relative, blockData.get(x, y, z.pos), skyData.get(x, y, z.pos));
			}
		}
	}

	private void collectCenter(LayerLightEventListener blockLight, LayerLightEventListener skyLight, long ptr, long section) {
		var pos = SectionPos.of(section);
		var blockData = blockLight.getDataLayerData(pos);
		var skyData = skyLight.getDataLayerData(pos);
		if (blockData == null || skyData == null) {
			return;
		}
		for (int y = 0; y < 16; y++) {
			for (int z = 0; z < 16; z++) {
				for (int x = 0; x < 16; x++) {
					write(ptr, x, y, z, blockData.get(x, y, z), skyData.get(x, y, z));
				}
			}
		}
	}

	private void collectCorners(LayerLightEventListener blockLight, LayerLightEventListener skyLight, long ptr, long section) {
		var blockPos = new BlockPos.MutableBlockPos();
		int xMin = SectionPos.sectionToBlockCoord(SectionPos.x(section));
		int yMin = SectionPos.sectionToBlockCoord(SectionPos.y(section));
		int zMin = SectionPos.sectionToBlockCoord(SectionPos.z(section));

		for (SectionEdge x : SectionEdge.values()) {
			for (SectionEdge y : SectionEdge.values()) {
				for (SectionEdge z : SectionEdge.values()) {
					blockPos.set(x.relative + xMin, y.relative + yMin, z.relative + zMin);
					write(ptr, x.relative, y.relative, z.relative, blockLight.getLightValue(blockPos), skyLight.getLightValue(blockPos));
				}
			}
		}
	}

	/**
	 * Write to the given section.
	 * @param ptr Pointer to the base of a section's data.
	 * @param x X coordinate in the section, from [-1, 16].
	 * @param y Y coordinate in the section, from [-1, 16].
	 * @param z Z coordinate in the section, from [-1, 16].
	 * @param block The block light level, from [0, 15].
	 * @param sky The sky light level, from [0, 15].
	 */
	private void write(long ptr, int x, int y, int z, int block, int sky) {
		int x1 = x + 1;
		int y1 = y + 1;
		int z1 = z + 1;

		int offset = x1 + z1 * 18 + y1 * 18 * 18;

		long packedByte = (block & 0xF) | ((sky & 0xF) << 4);

		MemoryUtil.memPutByte(ptr + offset, (byte) packedByte);
	}

	/**
	 * Get a pointer to the base of the given section.
	 * <p> If the section is not yet reserved, allocate a chunk in the arena.
	 * @param section The section to write to.
	 * @return A raw pointer to the base of the section.
	 */
	private long ptrForSection(long section) {
		return arena.indexToPointer(indexForSection(section));
	}

	private int indexForSection(long section) {
		int out = section2ArenaIndex.get(section);

		// Need to allocate.
		if (out == INVALID_SECTION) {
			out = arena.alloc();
			section2ArenaIndex.put(section, out);
			needsLutRebuild = true;
		}
		return out;
	}

	public void delete() {
		arena.delete();
	}

	public boolean checkNeedsLutRebuildAndClear() {
		var out = needsLutRebuild;
		needsLutRebuild = false;
		return out;
	}

	public void uploadChangedSections(StagingBuffer staging, int dstVbo) {
		for (int i = changed.nextSetBit(0); i >= 0; i = changed.nextSetBit(i + 1)) {
			staging.enqueueCopy(arena.indexToPointer(i), SECTION_SIZE_BYTES, dstVbo, i * SECTION_SIZE_BYTES);
		}
		changed.clear();
	}

	public void upload(GlBuffer buffer) {
		if (changed.isEmpty()) {
			return;
		}

		buffer.upload(arena.indexToPointer(0), arena.capacity() * SECTION_SIZE_BYTES);
		changed.clear();
	}

	public IntArrayList createLut() {
		// TODO: incremental lut updates
		return LightLut.buildLut(section2ArenaIndex);
	}

	private enum SectionEdge {
		LOW(15, -1, -1),
		HIGH(0, 16, 1),
		;

		/**
		 * The position in the section to collect.
		 */
		private final int pos;
		/**
		 * The position relative to the main section.
		 */
		private final int relative;
		/**
		 * The offset to the neighboring section.
		 */
		private final int sectionOffset;

		SectionEdge(int pos, int relative, int sectionOffset) {
			this.pos = pos;
			this.relative = relative;
			this.sectionOffset = sectionOffset;
		}
	}
}
