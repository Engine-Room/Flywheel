package com.jozufozu.flywheel.impl.instancing.manager;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.controller.InstanceContext;
import com.jozufozu.flywheel.backend.BackendUtil;
import com.jozufozu.flywheel.impl.instancing.InstancingControllerHelper;
import com.jozufozu.flywheel.impl.instancing.storage.One2OneStorage;
import com.jozufozu.flywheel.impl.instancing.storage.Storage;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class EntityInstanceManager extends InstanceManager<Entity> {
	private final EntityStorage storage;

	public EntityInstanceManager(Engine engine) {
		storage = new EntityStorage(engine);
	}

	@Override
	protected Storage<Entity> getStorage() {
		return storage;
	}

	@Override
	protected boolean canCreateInstance(Entity entity) {
		if (!entity.isAlive()) {
			return false;
		}

		if (!InstancingControllerHelper.canInstance(entity)) {
			return false;
		}

		Level level = entity.level;

		return BackendUtil.isFlywheelLevel(level);
	}

	private static class EntityStorage extends One2OneStorage<Entity> {
		public EntityStorage(Engine engine) {
			super(engine);
		}

		@Override
		@Nullable
		protected Instance createRaw(Entity obj) {
			var controller = InstancingControllerHelper.getController(obj);
			if (controller == null) {
				return null;
			}

			return controller.createInstance(new InstanceContext(engine, engine.renderOrigin()), obj);
		}
	}
}
