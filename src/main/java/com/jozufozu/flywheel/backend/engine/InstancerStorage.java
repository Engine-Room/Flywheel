package com.jozufozu.flywheel.backend.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.model.Model;

public abstract class InstancerStorage<N extends AbstractInstancer<?>> {
	/**
	 * A map of instancer keys to instancers.
	 * <br>
	 * This map is populated as instancers are requested and contains both initialized and uninitialized instancers.
	 * Write access to this map must be synchronized on {@link #creationLock}.
	 * <br>
	 * See {@link #getInstancer} for insertion details.
	 */
	protected final Map<InstancerKey<?>, N> instancers = new HashMap<>();
	/**
	 * A list of instancers that have not yet been initialized.
	 * <br>
	 * All new instancers land here before having resources allocated in {@link #flush}.
	 * Write access to this list must be synchronized on {@link #creationLock}.
	 */
	protected final List<UninitializedInstancer<N, ?>> uninitializedInstancers = new ArrayList<>();
	/**
	 * Mutex for {@link #instancers} and {@link #uninitializedInstancers}.
	 */
	protected final Object creationLock = new Object();

	@SuppressWarnings("unchecked")
	public <I extends Instance> Instancer<I> getInstancer(InstanceType<I> type, Model model, RenderStage stage) {
		InstancerKey<I> key = new InstancerKey<>(type, model, stage);

		N instancer = instancers.get(key);
		// Happy path: instancer is already initialized.
		if (instancer != null) {
			return (Instancer<I>) instancer;
		}

		// Unhappy path: instancer is not initialized, need to sync to make sure we don't create duplicates.
		synchronized (creationLock) {
			// Someone else might have initialized it while we were waiting for the lock.
			instancer = instancers.get(key);
			if (instancer != null) {
				return (Instancer<I>) instancer;
			}

			maybeWarnEmptyModel(model);

			// Create a new instancer and add it to the uninitialized list.
			instancer = create(type);
			instancers.put(key, instancer);
			uninitializedInstancers.add(new UninitializedInstancer<>(key, instancer, model, stage));
			return (Instancer<I>) instancer;
		}
	}

	private static void maybeWarnEmptyModel(Model model) {
		if (!model.meshes()
				.isEmpty()) {
			// All is good.
			return;
		}

		StringBuilder builder = new StringBuilder();
		builder.append("Creating an instancer for a model with no meshes! Stack trace:");
		StackWalker.getInstance()
				.walk(s -> s.skip(3)) // skip 3 to get back to the caller of InstancerProvider#instancer
				.forEach(f -> builder.append("\n\t")
						.append(f.toString()));

		Flywheel.LOGGER.warn(builder.toString());
	}

	public void delete() {
		instancers.clear();
		uninitializedInstancers.clear();
	}

	public void flush() {
		for (var instancer : uninitializedInstancers) {
			add(instancer.key(), instancer.instancer(), instancer.model(), instancer.stage());
		}
		uninitializedInstancers.clear();
	}

	public void onRenderOriginChanged() {
		instancers.values()
				.forEach(AbstractInstancer::clear);
	}

	protected abstract <I extends Instance> N create(InstanceType<I> type);

	protected abstract <I extends Instance> void add(InstancerKey<I> key, N instancer, Model model, RenderStage stage);

	private record UninitializedInstancer<N, I extends Instance>(InstancerKey<I> key, N instancer, Model model, RenderStage stage) {
	}
}
