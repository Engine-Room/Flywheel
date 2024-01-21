package com.jozufozu.flywheel.impl.visualization.manager;

import java.util.function.Supplier;

import com.jozufozu.flywheel.api.visual.Effect;
import com.jozufozu.flywheel.api.visual.EffectVisual;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.impl.visualization.storage.Storage;

public class EffectStorage extends Storage<Effect> {
	public EffectStorage(Supplier<VisualizationContext> visualizationContextSupplier) {
		super(visualizationContextSupplier);
	}

	@Override
	protected EffectVisual<?> createRaw(Effect obj) {
		return obj.visualize(visualizationContextSupplier.get());
	}

	@Override
	public boolean willAccept(Effect obj) {
		return true;
	}
}
