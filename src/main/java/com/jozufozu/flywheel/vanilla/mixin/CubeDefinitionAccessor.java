package com.jozufozu.flywheel.vanilla.mixin;

import java.util.Set;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.model.geom.builders.CubeDefinition;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.core.Direction;

@Mixin(CubeDefinition.class)
public interface CubeDefinitionAccessor {
	@Accessor("origin")
	Vector3f vanillin$origin();

	@Accessor("dimensions")
	Vector3f vanillin$dimensions();

	@Accessor("grow")
	CubeDeformation vanillin$grow();

	@Accessor("mirror")
	boolean vanillin$mirror();

	@Accessor("texCoord")
	UVPair vanillin$texCoord();

	@Accessor("texScale")
	UVPair vanillin$texScale();

	@Accessor("visibleFaces")
	Set<Direction> vanillin$visibleFaces();
}
