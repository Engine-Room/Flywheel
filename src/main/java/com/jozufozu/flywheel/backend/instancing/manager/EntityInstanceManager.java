package com.jozufozu.flywheel.backend.instancing.manager;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.controller.InstanceContext;
import com.jozufozu.flywheel.api.instance.controller.InstancingControllerRegistry;
import com.jozufozu.flywheel.backend.BackendUtil;
import com.jozufozu.flywheel.backend.instancing.storage.One2OneStorage;
import com.jozufozu.flywheel.backend.instancing.storage.Storage;
import com.jozufozu.flywheel.lib.instance.InstancingControllerHelper;

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

		if (!InstancingControllerHelper.canInstance(entity.getType())) {
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
			var controller = InstancingControllerRegistry.getController(InstancingControllerHelper.getType(obj));
			if (controller == null) {
				return null;
			}
			return controller.createInstance(new InstanceContext(engine, engine.renderOrigin()), obj);
		}
	}
}
