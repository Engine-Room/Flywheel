package com.jozufozu.flywheel.extension;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.visualization.EntityVisualizer;

import net.minecraft.world.entity.Entity;

public interface EntityTypeExtension<T extends Entity> {
	@Nullable
	EntityVisualizer<? super T> flywheel$getVisualizer();

	void flywheel$setVisualizer(@Nullable EntityVisualizer<? super T> visualizer);
}
