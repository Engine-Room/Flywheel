package dev.engine_room.flywheel.impl.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import dev.engine_room.flywheel.api.visualization.EntityVisualizer;
import dev.engine_room.flywheel.impl.extension.EntityTypeExtension;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

@Mixin(EntityType.class)
abstract class EntityTypeMixin<T extends Entity> implements EntityTypeExtension<T> {
	@Unique
	@Nullable
	private EntityVisualizer<? super T> flywheel$visualizer;

	@Override
	@Nullable
	public EntityVisualizer<? super T> flywheel$getVisualizer() {
		return flywheel$visualizer;
	}

	@Override
	public void flywheel$setVisualizer(@Nullable EntityVisualizer<? super T> visualizer) {
		flywheel$visualizer = visualizer;
	}
}
