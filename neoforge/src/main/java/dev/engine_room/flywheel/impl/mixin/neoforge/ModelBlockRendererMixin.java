package dev.engine_room.flywheel.impl.mixin.neoforge;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.lib.model.baked.NeoForgeMeshEmitter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;

@Mixin(ModelBlockRenderer.class)
abstract class ModelBlockRendererMixin {
	@Inject(method = "tesselateWithAO(Lnet/minecraft/world/level/BlockAndTintGetter;Ljava/util/List;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/function/Function;ZI)V", at = @At(value = "FIELD", target = "net/minecraft/client/renderer/block/ModelBlockRenderer.DIRECTIONS : [Lnet/minecraft/core/Direction;", opcode = Opcodes.GETSTATIC), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/extensions/BlockModelPartExtension;getRenderType(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/chunk/ChunkSectionLayer;")))
	private void onTesselateWithAO(CallbackInfo ci, @Local VertexConsumer vertexConsumer, @Local(ordinal = 0) boolean ao) {
		if (vertexConsumer instanceof NeoForgeMeshEmitter meshEmitter) {
			meshEmitter.prepareForPart(ao);
		}
	}
}
