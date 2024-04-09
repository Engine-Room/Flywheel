package com.jozufozu.flywheel.vanilla.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MaterialDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;

@Mixin(LayerDefinition.class)
public interface LayerDefinitionAccessor {
	@Accessor("mesh")
	MeshDefinition vanillin$mesh();

	@Accessor("material")
	MaterialDefinition vanillin$material();
}
