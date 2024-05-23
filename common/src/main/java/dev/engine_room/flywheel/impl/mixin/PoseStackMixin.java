package dev.engine_room.flywheel.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.impl.extension.PoseStackExtension;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;

@Mixin(PoseStack.class)
abstract class PoseStackMixin implements PoseStackExtension {
	@Unique
	private PoseTransformStack flywheel$wrapper;

	@Override
	public PoseTransformStack flywheel$transformStack() {
		if (flywheel$wrapper == null) {
			// Thread safety: bless you if you're calling this from multiple threads, but as there is no state
			// associated with the wrapper itself it's fine if we create multiple instances and one wins.
			flywheel$wrapper = new PoseTransformStack((PoseStack) (Object) this);
		}
		return flywheel$wrapper;
	}
}
