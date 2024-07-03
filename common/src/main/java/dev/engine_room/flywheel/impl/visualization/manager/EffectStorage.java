package dev.engine_room.flywheel.impl.visualization.manager;

import java.util.List;
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
	protected List<? extends EffectVisual<?>> createRaw(Effect obj, float partialTick) {
		return obj.visualize(visualizationContextSupplier.get(), partialTick);
	}

	@Override
	public boolean willAccept(Effect obj) {
		return true;
	}
}
