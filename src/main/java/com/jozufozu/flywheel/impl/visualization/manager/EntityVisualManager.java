package com.jozufozu.flywheel.impl.visualization.manager;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.visual.Visual;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.impl.visualization.VisualizationHelper;
import com.jozufozu.flywheel.impl.visualization.storage.Storage;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class EntityVisualManager extends AbstractVisualManager<Entity> {
	private final EntityStorage storage;

	public EntityVisualManager(Engine engine) {
		storage = new EntityStorage(engine);
	}

	@Override
	protected Storage<Entity> getStorage() {
		return storage;
	}

	private static class EntityStorage extends Storage<Entity> {
		public EntityStorage(Engine engine) {
			super(engine);
		}

		@Override
		@Nullable
		protected Visual createRaw(Entity obj) {
			var visualizer = VisualizationHelper.getVisualizer(obj);
			if (visualizer == null) {
				return null;
			}

			return visualizer.createVisual(new VisualizationContext(engine, engine.renderOrigin()), obj);
		}

		@Override
		public boolean willAccept(Entity entity) {
			if (!entity.isAlive()) {
				return false;
			}

			if (!VisualizationHelper.canVisualize(entity)) {
				return false;
			}

			Level level = entity.level;
			return level != null;
		}
	}
}
