package dev.engine_room.flywheel.backend.engine;

import dev.engine_room.flywheel.backend.engine.embed.AbstractEmbeddedEnvironment;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;

public class EnvironmentStorage {
	protected final ReferenceSet<AbstractEmbeddedEnvironment> environments = ReferenceSets.synchronize(new ReferenceLinkedOpenHashSet<>());

	public void track(AbstractEmbeddedEnvironment environment) {
		environments.add(environment);
	}

	public void flush() {
		environments.forEach(AbstractEmbeddedEnvironment::flush);
	}

	public void delete() {
		environments.clear();
	}
}
