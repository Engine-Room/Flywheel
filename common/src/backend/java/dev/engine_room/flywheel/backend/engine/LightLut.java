package dev.engine_room.flywheel.backend.engine;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.core.SectionPos;

public final class LightLut {
	private static final LongComparator SECTION_X_THEN_Y_THEN_Z = (long a, long b) -> {
		final var xComp = Integer.compare(SectionPos.x(a), SectionPos.x(b));
		if (xComp != 0) {
			return xComp;
		}
		var yComp = Integer.compare(SectionPos.y(a), SectionPos.y(b));
		if (yComp != 0) {
			return yComp;
		}
		return Integer.compare(SectionPos.z(a), SectionPos.z(b));
	};

	private LightLut() {
	}

	// Massive kudos to RogueLogix for figuring out this LUT scheme.
	// TODO: switch to y x z or x z y ordering
	// DATA LAYOUT
	// [0] : base chunk X, X index count, followed by linear indices of y blocks
	// [yBlockIndex] : baseChunk Y, Y index count, followed by linear indices of z blocks for this x
	// [zBlockIndex] : baseChunk Z, Z index count, followed by linear indices of lighting chunks
	// this data layout allows a single buffer to represent the lighting volume, without requiring the entire 3d lookup volume to be allocated
	public static IntArrayList buildLut(Long2IntMap sectionIndicesMaps) {
		if (sectionIndicesMaps.isEmpty()) {
			return new IntArrayList();
		}
		final var positions = sortedKeys(sectionIndicesMaps);
		final var baseX = SectionPos.x(positions.getLong(0));

		return buildLut(baseX, buildIndices(sectionIndicesMaps, positions, baseX));
	}

	private static ReferenceArrayList<IntObjectPair<ReferenceArrayList<IntArrayList>>> buildIndices(Long2IntMap sectionIndicesMaps, LongArrayList positions, int baseX) {
		final var indices = new ReferenceArrayList<IntObjectPair<ReferenceArrayList<IntArrayList>>>();
		for (long position : positions) {
			final var x = SectionPos.x(position);
			final var y = SectionPos.y(position);
			final var z = SectionPos.z(position);

			final var xIndex = x - baseX;
			if (indices.size() <= xIndex) {
				indices.ensureCapacity(xIndex + 1);
				indices.size(xIndex + 1);
			}
			var yLookup = indices.get(xIndex);
			if (yLookup == null) {
				//noinspection SuspiciousNameCombination
				yLookup = new IntObjectImmutablePair<>(y, new ReferenceArrayList<>());
				indices.set(xIndex, yLookup);
			}

			final var yIndices = yLookup.right();
			final var yIndex = y - yLookup.leftInt();
			if (yIndices.size() <= yIndex) {
				yIndices.ensureCapacity(yIndex + 1);
				yIndices.size(yIndex + 1);
			}
			var zLookup = yIndices.get(yIndex);
			if (zLookup == null) {
				zLookup = new IntArrayList();
				zLookup.add(z);
				zLookup.add(0); // this value will be filled in later
				yIndices.set(yIndex, zLookup);
			}

			final var zIndex = z - zLookup.getInt(0);
			if ((zLookup.size() - 2) <= zIndex) {
				zLookup.ensureCapacity(zIndex + 3);
				zLookup.size(zIndex + 3);
			}
			// Add 1 to the actual index so that 0 indicates a missing section.
			zLookup.set(zIndex + 2, sectionIndicesMaps.get(position) + 1);
		}
		return indices;
	}

	private static @NotNull LongArrayList sortedKeys(Long2IntMap sectionIndicesMaps) {
		final var out = new LongArrayList(sectionIndicesMaps.keySet());
		out.unstableSort(SECTION_X_THEN_Y_THEN_Z);
		return out;
	}

	private static IntArrayList buildLut(int baseX, ReferenceArrayList<IntObjectPair<ReferenceArrayList<IntArrayList>>> indices) {
		final var out = new IntArrayList();
		out.add(baseX);
		out.add(indices.size());
		for (int i = 0; i < indices.size(); i++) {
			out.add(0);
		}
		for (int x = 0; x < indices.size(); x++) {
			final var yLookup = indices.get(x);
			if (yLookup == null) {
				out.set(x + 2, 0);
				continue;
			}
			// ensure that the base position and size dont cross a (64 byte) cache line
			if ((out.size() & 0xF) == 0xF) {
				out.add(0);
			}

			final var baseYIndex = out.size();
			out.set(x + 2, baseYIndex);

			final var yIndices = yLookup.right();
			out.add(yLookup.leftInt());
			out.add(yIndices.size());
			for (int i = 0; i < indices.size(); i++) {
				out.add(0);
			}

			for (int y = 0; y < yIndices.size(); y++) {
				final var zLookup = yIndices.get(y);
				if (zLookup == null) {
					out.set(baseYIndex + y + 2, 0);
					continue;
				}
				// ensure that the base position and size dont cross a (64 byte) cache line
				if ((out.size() & 0xF) == 0xF) {
					out.add(0);
				}
				out.set(baseYIndex + y + 2, out.size());
				zLookup.set(1, zLookup.size() - 2);
				out.addAll(zLookup);
			}
		}
		return out;
	}
}
