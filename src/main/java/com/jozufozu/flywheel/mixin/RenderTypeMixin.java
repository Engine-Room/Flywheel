package com.jozufozu.flywheel.mixin;

import javax.annotation.Nonnull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.jozufozu.flywheel.backend.instancing.DrawBuffer;
import com.jozufozu.flywheel.backend.instancing.RenderTypeExtension;

import net.minecraft.client.renderer.RenderType;

@Mixin(RenderType.class)
public class RenderTypeMixin implements RenderTypeExtension {

	@Unique
	private final DrawBuffer flywheel$drawBuffer = new DrawBuffer((RenderType) (Object) this);

	@Override
	@Nonnull
	public DrawBuffer flywheel$getDrawBuffer() {
		return flywheel$drawBuffer;
	}
}
