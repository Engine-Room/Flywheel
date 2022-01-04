package com.jozufozu.flywheel.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.jozufozu.flywheel.backend.instancing.entity.EntityInstancingController;
import com.jozufozu.flywheel.backend.instancing.entity.EntityTypeExtension;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

@Mixin(EntityType.class)
public class EntityTypeMixin<T extends Entity> implements EntityTypeExtension<T> {
	@Unique
	private EntityInstancingController<? super T> flywheel$instancingController;

	@Override
	@Nullable
	public EntityInstancingController<? super T> flywheel$getInstancingController() {
		return flywheel$instancingController;
	}

	@Override
	public void flywheel$setInstancingController(@Nullable EntityInstancingController<? super T> instancingController) {
		this.flywheel$instancingController = instancingController;
	}
}
