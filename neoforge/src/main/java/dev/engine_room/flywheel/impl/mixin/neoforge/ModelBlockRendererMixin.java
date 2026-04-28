package dev.engine_room.flywheel.impl.mixin.neoforge;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.renderer.block.ModelBlockRenderer;

@Mixin(ModelBlockRenderer.class)
abstract class ModelBlockRendererMixin {
	@Inject(method = "tesselateAmbientOcclusion", at = @At(value = "FIELD", target = "net/minecraft/client/renderer/block/ModelBlockRenderer.DIRECTIONS : [Lnet/minecraft/core/Direction;", opcode = Opcodes.GETSTATIC))
	private void onTesselateWithAO(CallbackInfo ci, @Local(name = "ao") boolean ao) {
		// TODO - IThundxr - This needs some thinking, reimplementing this isn't exactly easy at the moment
//		if (vertexConsumer instanceof NeoForgeMeshEmitter meshEmitter) {
//			meshEmitter.prepareForPart(ao);
//		}
	}
}
