package com.jozufozu.flywheel.vanilla.mixin;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

@Mixin(PartDefinition.class)
public interface PartDefinitionAccessor {
	@Accessor("cubes")
	List<CubeDefinition> vanillin$cubes();

	@Accessor("partPose")
	PartPose vanillin$partPose();

	@Accessor("children")
	Map<String, PartDefinition> vanillin$children();
}
