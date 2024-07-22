package dev.engine_room.flywheel.impl.visualization.storage;

import dev.engine_room.flywheel.api.visual.EntityVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class EntityStorage extends Storage<Entity> {
	public EntityStorage(VisualizationContext visualizationContext) {
		super(visualizationContext);
	}

	@Override
	protected EntityVisual<?> createRaw(Entity obj, float partialTick) {
		var visualizer = VisualizationHelper.getVisualizer(obj);
		if (visualizer == null) {
			return null;
		}

		return visualizer.createVisual(visualizationContext, obj, partialTick);
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
