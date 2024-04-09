package com.jozufozu.flywheel.vanilla.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.model.geom.builders.MaterialDefinition;

@Mixin(MaterialDefinition.class)
public interface MaterialDefinitionAccessor {
	@Accessor("xTexSize")
	int vanillin$xTexSize();

	@Accessor("yTexSize")
	int vanillin$yTexSize();
}
