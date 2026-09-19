package dev.engine_room.flywheel.backend.engine;

import java.util.BitSet;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.model.LineModelBuilder;

import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.visual.Effect;
import dev.engine_room.flywheel.api.visual.EffectVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.backend.BackendDebugFlags;
import dev.engine_room.flywheel.backend.engine.indirect.StagingBuffer;
import dev.engine_room.flywheel.backend.gl.buffer.GlBuffer;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.math.MoreMath;
import dev.engine_room.flywheel.lib.task.SimplePlan;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.lib.visual.util.InstanceRecycler;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.LevelAccessor;

/**
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
public class LightStorage implements Effect {
	public static final int BLOCKS_PER_SECTION = 18 * 18 * 18;
	public static final int LIGHT_SIZE_BYTES = BLOCKS_PER_SECTION;
	public static final int SOLID_SIZE_BYTES = MoreMath.ceilingDiv(BLOCKS_PER_SECTION, Integer.SIZE) * Integer.BYTES;
	public static final int SECTION_SIZE_BYTES = SOLID_SIZE_BYTES + LIGHT_SIZE_BYTES;
	private static final int DEFAULT_ARENA_CAPACITY_SECTIONS = 64;
	private static final int INVALID_SECTION = -1;

	private final LevelAccessor level;
	private final LightLut lut;
	public final CpuArena arena;
	private final Long2IntMap section2ArenaIndex;
	private final LightDataCollector collector;

	private final BitSet changed = new BitSet();
	private boolean needsLutRebuild = false;
	private boolean isDebugOn = false;

	private final LongSet updatedSections = new LongOpenHashSet();
	@Nullable
	private LongSet requestedSections;

	public LightStorage(LevelAccessor level) {
		this.level = level;
		lut = new LightLut();
		arena = new CpuArena(SECTION_SIZE_BYTES, DEFAULT_ARENA_CAPACITY_SECTIONS);
		section2ArenaIndex = new Long2IntOpenHashMap();
		section2ArenaIndex.defaultReturnValue(INVALID_SECTION);
		collector = LightDataCollector.of(level);
	}

	@Override
	public LevelAccessor level() {
		return level;
	}

	@Override
	public EffectVisual<?> visualize(VisualizationContext ctx, float partialTick) {
		return new DebugVisual(ctx, partialTick);
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

	public void onLightUpdate(long section) {
		updatedSections.add(section);
	}

	public <C> Plan<C> createFramePlan() {
		return SimplePlan.of(() -> {
			if (BackendDebugFlags.LIGHT_STORAGE_VIEW != isDebugOn) {
				var visualizationManager = VisualizationManager.get(level);

				// Really should be non-null, but just in case.
				if (visualizationManager != null) {
					if (BackendDebugFlags.LIGHT_STORAGE_VIEW) {
						visualizationManager.effects()
								.queueAdd(this);
					} else {
						visualizationManager.effects()
								.queueRemove(this);
					}
				}
				isDebugOn = BackendDebugFlags.LIGHT_STORAGE_VIEW;
			}

			if (updatedSections.isEmpty() && requestedSections == null) {
				return;
			}

			removeUnusedSections();

			// Start building the set of sections we need to collect this frame.
			LongSet sectionsToCollect;
			if (requestedSections == null) {
				// If none were requested, then we need to collect all sections that received updates.
				sectionsToCollect = new LongOpenHashSet();
			} else {
				// If we did receive a new set of requested sections, we only
				// need to collect the sections that weren't yet tracked.
				sectionsToCollect = new LongOpenHashSet(requestedSections);
				sectionsToCollect.removeAll(section2ArenaIndex.keySet());
			}

			// updatedSections contains all sections that received light updates,
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
			sectionsToCollect.forEach(this::collectSection);

			updatedSections.clear();
			requestedSections = null;
		});
	}

	private void removeUnusedSections() {
		if (requestedSections == null) {
			return;
		}

		boolean anyRemoved = false;

		var entries = section2ArenaIndex.long2IntEntrySet();
		var it = entries.iterator();
		while (it.hasNext()) {
			var entry = it.next();
			var section = entry.getLongKey();

			if (!requestedSections.contains(section)) {
				arena.free(entry.getIntValue());
				endTrackingSection(section);
				it.remove();
				anyRemoved = true;
			}
		}

		if (anyRemoved) {
			lut.prune();
			needsLutRebuild = true;
		}
	}

	private void beginTrackingSection(long section, int index) {
		lut.add(section, index);
		needsLutRebuild = true;
	}

	private void endTrackingSection(long section) {
		lut.remove(section);
		needsLutRebuild = true;
	}

	public int capacity() {
		return arena.capacity();
	}

	public void collectSection(long section) {
		int index = indexForSection(section);

		changed.set(index);

		long ptr = arena.indexToPointer(index);

		// Zero it out first. This is basically free and makes it easier to handle missing sections later.
		MemoryUtil.memSet(ptr, 0, SECTION_SIZE_BYTES);

		collector.collectSection(ptr, section);
	}

	private int indexForSection(long section) {
		int out = section2ArenaIndex.get(section);

		// Need to allocate.
		if (out == INVALID_SECTION) {
			out = arena.alloc();
			section2ArenaIndex.put(section, out);
			beginTrackingSection(section, out);
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
		return lut.flatten();
	}

	public class DebugVisual implements EffectVisual<LightStorage>, SimpleDynamicVisual {
		//    010------110
		//    /|       /|
		//   / |      / |
		// 011------111 |
		//  |  |     |  |
		//  | 000----|-100
		//  | /      | /
		//  |/       |/
		// 001------101
		public static final Model BOX_MODEL = new LineModelBuilder(12)
				// Starting from 0, 0, 0
				.line(0, 0, 0, 0, 0, 1)
				.line(0, 0, 0, 0, 1, 0)
				.line(0, 0, 0, 1, 0, 0)
				// Starting from 0, 1, 1
				.line(0, 1, 1, 0, 1, 0)
				.line(0, 1, 1, 0, 0, 1)
				.line(0, 1, 1, 1, 1, 1)
				// Starting from 1, 0, 1
				.line(1, 0, 1, 1, 0, 0)
				.line(1, 0, 1, 1, 1, 1)
				.line(1, 0, 1, 0, 0, 1)
				// Starting from 1, 1, 0
				.line(1, 1, 0, 1, 1, 1)
				.line(1, 1, 0, 1, 0, 0)
				.line(1, 1, 0, 0, 1, 0)
				.build();

		private final InstanceRecycler<TransformedInstance> boxes;
		private final Vec3i renderOrigin;

		public DebugVisual(VisualizationContext ctx, float partialTick) {
			renderOrigin = ctx.renderOrigin();
			boxes = new InstanceRecycler<>(() -> ctx.instancerProvider()
					.instancer(InstanceTypes.TRANSFORMED, BOX_MODEL)
					.createInstance());
		}

		@Override
		public void beginFrame(Context ctx) {
			boxes.resetCount();

			setupSectionBoxes();
			setupLutRangeBoxes();

			boxes.discardExtra();
		}

		private void setupSectionBoxes() {
			section2ArenaIndex.keySet()
					.forEach(l -> {
						var x = SectionPos.x(l) * 16 - renderOrigin.getX();
						var y = SectionPos.y(l) * 16 - renderOrigin.getY();
						var z = SectionPos.z(l) * 16 - renderOrigin.getZ();

						var instance = boxes.get();

						// Slightly smaller than a full 16x16x16 section to make it obvious which sections
						// are actually represented when many are tiled next to each other.
						instance.setIdentityTransform()
								.translate(x + 1, y + 1, z + 1)
								.scale(14)
								.color(255, 255, 0)
								.light(LightCoordsUtil.FULL_BRIGHT)
								.setChanged();
					});
		}

		private void setupLutRangeBoxes() {
			var first = lut.indices;

			var base1 = first.base();
			var size1 = first.size();

			float debug1 = base1 * 16 - renderOrigin.getY();

			float min2 = Float.POSITIVE_INFINITY;
			float max2 = Float.NEGATIVE_INFINITY;

			float min3 = Float.POSITIVE_INFINITY;
			float max3 = Float.NEGATIVE_INFINITY;

			for (int y = 0; y < size1; y++) {
				var second = first.getRaw(y);

				if (second == null) {
					continue;
				}

				var base2 = second.base();
				var size2 = second.size();

				float y2 = (base1 + y) * 16 - renderOrigin.getY() + 7.5f;

				min2 = Math.min(min2, base2);
				max2 = Math.max(max2, base2 + size2);

				float minLocal3 = Float.POSITIVE_INFINITY;
				float maxLocal3 = Float.NEGATIVE_INFINITY;

				float debug2 = base2 * 16 - renderOrigin.getX();

				for (int x = 0; x < size2; x++) {
					var third = second.getRaw(x);

					if (third == null) {
						continue;
					}

					var base3 = third.base();
					var size3 = third.size();

					float x2 = (base2 + x) * 16 - renderOrigin.getX() + 7.5f;

					min3 = Math.min(min3, base3);
					max3 = Math.max(max3, base3 + size3);

					minLocal3 = Math.min(minLocal3, base3);
					maxLocal3 = Math.max(maxLocal3, base3 + size3);

					float debug3 = base3 * 16 - renderOrigin.getZ();

					for (int z = 0; z < size3; z++) {
						boxes.get()
								.setIdentityTransform()
								.translate(x2, y2, debug3)
								.scale(1, 1, size3 * 16)
								.color(0, 0, 255)
								.light(LightCoordsUtil.FULL_BRIGHT)
								.setChanged();
					}
				}

				boxes.get()
						.setIdentityTransform()
						.translate(debug2, y2, minLocal3 * 16 - renderOrigin.getZ())
						.scale(size2 * 16, 1, (maxLocal3 - minLocal3) * 16)
						.color(255, 0, 0)
						.light(LightCoordsUtil.FULL_BRIGHT)
						.setChanged();
			}

			boxes.get()
					.setIdentityTransform()
					.translate(min2 * 16 - renderOrigin.getX(), debug1, min3 * 16 - renderOrigin.getZ())
					.scale((max2 - min2) * 16, size1 * 16, (max3 - min3) * 16)
					.color(0, 255, 0)
					.light(LightCoordsUtil.FULL_BRIGHT)
					.setChanged();
		}

		@Override
		public void update(float partialTick) {

		}

		@Override
		public void delete() {
			boxes.delete();
		}
	}
}
