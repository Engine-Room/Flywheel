package com.jozufozu.flywheel.api.visualization;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.phys.AABB;

public interface EmbeddedLevel {
	void transform(PoseStack stack);

	AABB boundingBox();
}
