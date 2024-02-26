package com.jozufozu.flywheel.backend.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visualization.VisualEmbedding;

public abstract class InstancerStorage<N extends AbstractInstancer<?>> {
	/**
	 * A map of instancer keys to instancers.
	 * <br>
	 * This map is populated as instancers are requested and contains both initialized and uninitialized instancers.
	 */
	protected final Map<InstancerKey<?>, N> instancers = new ConcurrentHashMap<>();
	/**
	 * A list of instancers that have not yet been initialized.
	 * <br>
	 * All new instancers land here before having resources allocated in {@link #flush}.
	 */
	protected final List<UninitializedInstancer<N, ?>> initializationQueue = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public <I extends Instance> Instancer<I> getInstancer(@Nullable VisualEmbedding level, InstanceType<I> type, Model model, RenderStage stage) {
		return (Instancer<I>) instancers.computeIfAbsent(new InstancerKey<>(level, type, model, stage), this::createAndDeferInit);
	}

	public void delete() {
		instancers.clear();
		initializationQueue.clear();
	}

	public void flush() {
		// Thread safety: flush is called from the render thread after all visual updates have been made,
		// so there are no:tm: threads we could be racing with.
		for (var instancer : initializationQueue) {
			initialize(instancer.key(), instancer.instancer());
		}
		initializationQueue.clear();
	}

	public void onRenderOriginChanged() {
		instancers.values()
				.forEach(AbstractInstancer::clear);
	}

	protected abstract <I extends Instance> N create(InstancerKey<I> type);

	protected abstract <I extends Instance> void initialize(InstancerKey<I> key, N instancer);

	private N createAndDeferInit(InstancerKey<?> key) {
		var out = create(key);

		// Only queue the instancer for initialization if it has anything to render.
        if (checkAndWarnEmptyModel(key.model())) {
			// Thread safety: this method is called atomically from within computeIfAbsent,
			// so we don't need extra synchronization to protect the queue.
			initializationQueue.add(new UninitializedInstancer<>(key, out));
		}
        return out;
	}

	protected record UninitializedInstancer<N, I extends Instance>(InstancerKey<I> key, N instancer) {

	}

	private static boolean checkAndWarnEmptyModel(Model model) {
		if (!model.meshes().isEmpty()) {
			return true;
		}

		StringBuilder builder = new StringBuilder();
		builder.append("Creating an instancer for a model with no meshes! Stack trace:");

		StackWalker.getInstance()
				// .walk(s -> s.skip(3)) // this causes forEach to crash for some reason
				.forEach(f -> builder.append("\n\t")
						.append(f.toString()));

		Flywheel.LOGGER.warn(builder.toString());

		return false;
	}
}
