package com.jozufozu.flywheel.backend.engine;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.visualization.VisualEmbedding;
import com.jozufozu.flywheel.backend.engine.embed.EmbeddedEnvironment;

import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;

public class EnvironmentStorage {
	protected final ReferenceSet<EmbeddedEnvironment> environments = ReferenceSets.synchronize(new ReferenceLinkedOpenHashSet<>());
	private final Queue<EmbeddedEnvironment> forDeletion = new ConcurrentLinkedQueue<>();
	private final EngineImpl engine;

	public EnvironmentStorage(EngineImpl engine) {
		this.engine = engine;
	}

	public VisualEmbedding create(RenderStage stage) {
		var out = new EmbeddedEnvironment(engine, stage);

		environments.add(out);

		return out;
	}

	public void enqueueDeletion(EmbeddedEnvironment environment) {
		environments.remove(environment);

		forDeletion.add(environment);
	}

	public void flush() {
		EmbeddedEnvironment env;

		while ((env = forDeletion.poll()) != null) {
			env.actuallyDelete();
		}

		environments.forEach(EmbeddedEnvironment::flush);
	}

	public void delete() {
		environments.forEach(EmbeddedEnvironment::actuallyDelete);
		environments.clear();
	}
}
