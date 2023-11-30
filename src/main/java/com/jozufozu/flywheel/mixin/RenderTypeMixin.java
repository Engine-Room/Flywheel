package com.jozufozu.flywheel.mixin;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.jozufozu.flywheel.backend.engine.batching.DrawBufferSet;
import com.jozufozu.flywheel.extension.RenderTypeExtension;

import net.minecraft.client.renderer.RenderType;

@Mixin(RenderType.class)
abstract class RenderTypeMixin implements RenderTypeExtension {
	@Shadow
	@Final
	private boolean sortOnUpload;

	@Unique
	private DrawBufferSet flywheel$drawBufferSet;

	@Override
	@NotNull
	public DrawBufferSet flywheel$getDrawBufferSet() {
		if (flywheel$drawBufferSet == null) {
			flywheel$drawBufferSet = new DrawBufferSet((RenderType) (Object) this, sortOnUpload);
		}
		return flywheel$drawBufferSet;
	}
}
