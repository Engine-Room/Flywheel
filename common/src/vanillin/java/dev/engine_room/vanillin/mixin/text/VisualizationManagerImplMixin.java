package dev.engine_room.vanillin.mixin.text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.engine_room.flywheel.api.backend.Engine;
import dev.engine_room.flywheel.api.backend.RenderContext;
import dev.engine_room.flywheel.impl.visualization.VisualizationManagerImpl;
import dev.engine_room.vanillin.text.AsyncFontUploads;

/**
 * TODO: Add an api for this kind of hook? I feel it may need to be more inspect-able than just "list of runnables" though
 * Cursed self-mixin, but we need to execute stuff on the render thread AFTER visual updates and BEFORE render.
 */
@Mixin(value = VisualizationManagerImpl.class, remap = false)
public class VisualizationManagerImplMixin {
	/**
	 * Before calling into the engine, make sure we've flushed all font updates that happened off-thread.
	 */
	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Ldev/engine_room/flywheel/api/backend/Engine;render(Ldev/engine_room/flywheel/api/backend/RenderContext;)V"))
	private static void vanillin$executeFontUploads(Engine instance, RenderContext ctx, Operation<Void> original) {
		AsyncFontUploads.execute();
		original.call(instance, ctx);
	}
}
