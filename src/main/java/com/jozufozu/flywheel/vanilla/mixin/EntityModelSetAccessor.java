package com.jozufozu.flywheel.vanilla.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

@Mixin(EntityModelSet.class)
public interface EntityModelSetAccessor {
	@Accessor("roots")
	Map<ModelLayerLocation, LayerDefinition> vanillin$roots();
}
