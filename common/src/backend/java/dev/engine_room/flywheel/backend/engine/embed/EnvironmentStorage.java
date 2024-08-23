package dev.engine_room.flywheel.backend.engine.embed;

import dev.engine_room.flywheel.backend.engine.Arena;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;

public class EnvironmentStorage {
	public static final int MATRIX_SIZE_BYTES = (16 + 12) * Float.BYTES;

	protected final Object lock = new Object();

	protected final ReferenceSet<EmbeddedEnvironment> environments = new ReferenceLinkedOpenHashSet<>();

	// Note than the arena starts indexing at zero, but we reserve zero for the identity matrix.
	// Any time an ID from the arena is written we want to add one to it.
	public final Arena arena = new Arena(MATRIX_SIZE_BYTES, 32);

	{
		// Reserve the identity matrix. Burns a few bytes but oh well.
		arena.alloc();
	}

	public void track(EmbeddedEnvironment environment) {
		synchronized (lock) {
			if (environments.add(environment)) {
				environment.matrixIndex = arena.alloc();
			}
		}
	}

	public void flush() {
		environments.removeIf(embeddedEnvironment -> {
			var deleted = embeddedEnvironment.isDeleted();
			if (deleted && embeddedEnvironment.matrixIndex > 0) {
				arena.free(embeddedEnvironment.matrixIndex);
			}
			return deleted;
		});
		for (EmbeddedEnvironment environment : environments) {
			environment.flush(arena.indexToPointer(environment.matrixIndex));
		}
	}

	public void delete() {
		arena.delete();
	}
}
