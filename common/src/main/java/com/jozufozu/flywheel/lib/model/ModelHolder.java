package com.jozufozu.flywheel.lib.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.model.Model;

public class ModelHolder {
	private static final List<ModelHolder> ALL = new ArrayList<>();
	private final Supplier<Model> factory;
	@Nullable
	private volatile Model model;

	public ModelHolder(Supplier<Model> factory) {
		this.factory = factory;
		ALL.add(this);
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
					model.delete();
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
