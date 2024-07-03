package dev.engine_room.flywheel.impl.visualization.manager;

import java.util.List;
import java.util.function.Supplier;

import dev.engine_room.flywheel.api.visual.EntityVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.impl.visualization.storage.Storage;
import dev.engine_room.flywheel.lib.visual.VisualizationHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class EntityStorage extends Storage<Entity> {
	public EntityStorage(Supplier<VisualizationContext> visualizationContextSupplier) {
		super(visualizationContextSupplier);
	}

	@Override
    protected List<? extends EntityVisual<?>> createRaw(Entity obj, float partialTick) {
		var visualizer = VisualizationHelper.getVisualizer(obj);
		if (visualizer == null) {
			return List.of();
		}

		return visualizer.createVisual(visualizationContextSupplier.get(), obj, partialTick);
	}

	@Override
	public boolean willAccept(Entity entity) {
		if (!entity.isAlive()) {
			return false;
		}

		if (!VisualizationHelper.canVisualize(entity)) {
			return false;
		}

		Level level = entity.level();
		return level != null;
	}
}
