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
	private DrawBuffer flywheel$drawBuffer;

	@Override
	@Nonnull
	public DrawBuffer flywheel$getDrawBuffer() {
		if (flywheel$drawBuffer == null) {
			flywheel$drawBuffer = new DrawBuffer((RenderType) (Object) this);
		}
		return flywheel$drawBuffer;
	}
}
