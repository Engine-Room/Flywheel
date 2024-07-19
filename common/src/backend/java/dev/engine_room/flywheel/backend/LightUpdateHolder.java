package dev.engine_room.flywheel.backend;

import dev.engine_room.flywheel.lib.util.LevelAttached;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.level.LevelAccessor;

/**
 * Stores the set of updates light sections for LightStorage to poll in its frame plan.
 */
public class LightUpdateHolder {
	private static final LevelAttached<LightUpdateHolder> HOLDERS = new LevelAttached<>(level -> new LightUpdateHolder());

	private final LongSet updatedSections = new LongOpenHashSet();

	private LightUpdateHolder() {
	}

	public static LightUpdateHolder get(LevelAccessor level) {
		return HOLDERS.get(level);
	}

	public LongSet getAndClearUpdatedSections() {
		if (updatedSections.isEmpty()) {
			return LongSet.of();
		}

		var out = new LongArraySet(updatedSections);
		updatedSections.clear();
		return out;
	}

	public void add(long section) {
		updatedSections.add(section);
	}
}
