package dev.engine_room.flywheel.backend.engine.embed;

import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;

public class EnvironmentStorage {
	protected final ReferenceSet<EmbeddedEnvironment> environments = ReferenceSets.synchronize(new ReferenceLinkedOpenHashSet<>());

	public void track(EmbeddedEnvironment environment) {
		environments.add(environment);
	}

	public void flush() {
		environments.removeIf(EmbeddedEnvironment::isDeleted);
		environments.forEach(EmbeddedEnvironment::flush);
	}
}
