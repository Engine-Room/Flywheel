package dev.engine_room.flywheel.impl.extension;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.visualization.EntityVisualizer;

import net.minecraft.world.entity.Entity;

public interface EntityTypeExtension<T extends Entity> {
	@Nullable
	EntityVisualizer<? super T> flywheel$getVisualizer();

	void flywheel$setVisualizer(@Nullable EntityVisualizer<? super T> visualizer);
}
