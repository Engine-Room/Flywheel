package dev.engine_room.flywheel.lib.model;

import java.util.Set;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.util.FlwUtil;

public final class ModelHolder {
	private static final Set<ModelHolder> ALL = FlwUtil.createWeakHashSet();
	private final Supplier<Model> factory;
	@Nullable
	private volatile Model model;

	public ModelHolder(Supplier<Model> factory) {
		this.factory = factory;

		synchronized (ALL) {
			ALL.add(this);
		}
	}

	public Model get() {
		Model model = this.model;

		if (model == null) {
			synchronized (this) {
				model = this.model;
				if (model == null) {
					this.model = model = factory.get();
				}
			}
		}

		return model;
	}

	public void clear() {
		Model model = this.model;

		if (model != null) {
			synchronized (this) {
				model = this.model;
				if (model != null) {
					this.model = null;
				}
			}
		}
	}

	@ApiStatus.Internal
	public static void onEndClientResourceReload() {
		for (ModelHolder holder : ALL) {
			holder.clear();
		}
	}
}
