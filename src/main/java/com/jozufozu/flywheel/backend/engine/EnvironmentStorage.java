package com.jozufozu.flywheel.backend.engine;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.jozufozu.flywheel.backend.engine.embed.AbstractEmbeddedEnvironment;

import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;

public class EnvironmentStorage {
	protected final ReferenceSet<AbstractEmbeddedEnvironment> environments = ReferenceSets.synchronize(new ReferenceLinkedOpenHashSet<>());
	private final Queue<AbstractEmbeddedEnvironment> forDeletion = new ConcurrentLinkedQueue<>();

	public void track(AbstractEmbeddedEnvironment environment) {
		environments.add(environment);
	}

	public void enqueueDeletion(AbstractEmbeddedEnvironment environment) {
		environments.remove(environment);

		forDeletion.add(environment);
	}

	public void flush() {
		AbstractEmbeddedEnvironment env;

		while ((env = forDeletion.poll()) != null) {
			env.actuallyDelete();
		}

		environments.forEach(AbstractEmbeddedEnvironment::flush);
	}

	public void delete() {
		environments.forEach(AbstractEmbeddedEnvironment::actuallyDelete);
		environments.clear();
	}
}
