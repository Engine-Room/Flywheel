package com.jozufozu.flywheel.vanilla.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.model.geom.builders.CubeDeformation;

@Mixin(CubeDeformation.class)
public interface CubeDeformationAccessor {
	@Accessor("growX")
	float vanillin$growX();

	@Accessor("growY")
	float vanillin$growY();

	@Accessor("growZ")
	float vanillin$growZ();
}
