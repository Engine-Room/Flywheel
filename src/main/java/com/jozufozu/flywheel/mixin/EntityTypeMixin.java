package com.jozufozu.flywheel.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.jozufozu.flywheel.api.visualization.EntityVisualizer;
import com.jozufozu.flywheel.extension.EntityTypeExtension;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

@Mixin(EntityType.class)
abstract class EntityTypeMixin<T extends Entity> implements EntityTypeExtension<T> {
	@Unique
	private EntityVisualizer<? super T> flywheel$visualizer;

	@Override
	@Nullable
	public EntityVisualizer<? super T> flywheel$getVisualizer() {
		return flywheel$visualizer;
	}

	@Override
	public void flywheel$setVisualizer(@Nullable EntityVisualizer<? super T> visualizer) {
		this.flywheel$visualizer = visualizer;
	}
}
