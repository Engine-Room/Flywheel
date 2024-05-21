package com.jozufozu.flywheel.impl.visualization.manager;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.visual.Visual;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.visual.VisualizationHelper;
import com.jozufozu.flywheel.impl.visualization.storage.Storage;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class EntityStorage extends Storage<Entity> {
	public EntityStorage(Supplier<VisualizationContext> visualizationContextSupplier) {
		super(visualizationContextSupplier);
	}

	@Override
	@Nullable
	protected Visual createRaw(Entity obj) {
		var visualizer = VisualizationHelper.getVisualizer(obj);
		if (visualizer == null) {
			return null;
		}

		return visualizer.createVisual(visualizationContextSupplier.get(), obj);
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
