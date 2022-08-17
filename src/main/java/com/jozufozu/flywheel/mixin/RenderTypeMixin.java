package com.jozufozu.flywheel.mixin;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.jozufozu.flywheel.backend.instancing.batching.DrawBuffer;
import com.jozufozu.flywheel.extension.RenderTypeExtension;

import net.minecraft.client.renderer.RenderType;

@Mixin(RenderType.class)
public class RenderTypeMixin implements RenderTypeExtension {
	@Unique
	private DrawBuffer flywheel$drawBuffer;

	@Override
	@NotNull
	public DrawBuffer flywheel$getDrawBuffer() {
		if (flywheel$drawBuffer == null) {
			flywheel$drawBuffer = new DrawBuffer(((RenderType) (Object) this).format());
		}
		return flywheel$drawBuffer;
	}
}
