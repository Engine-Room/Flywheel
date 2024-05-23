package dev.engine_room.flywheel.impl.visualization.manager;

import java.util.function.Supplier;

import dev.engine_room.flywheel.api.visual.Effect;
import dev.engine_room.flywheel.api.visual.EffectVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.impl.visualization.storage.Storage;

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
