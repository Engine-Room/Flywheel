package dev.engine_room.flywheel.impl.mixin.neoforge;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import dev.engine_room.flywheel.lib.model.baked.BlockStateModelBufferer;
import dev.engine_room.flywheel.lib.model.baked.NeoForgeMeshEmitter;
import dev.engine_room.flywheel.lib.model.baked.NeoForgeMeshEmitterManager;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;

@Mixin(ModelBlockRenderer.class)
abstract class ModelBlockRendererMixin {
	@Inject(method = "tesselateAmbientOcclusion", at = @At(value = "FIELD", target = "net/minecraft/client/renderer/block/ModelBlockRenderer.DIRECTIONS : [Lnet/minecraft/core/Direction;", opcode = Opcodes.GETSTATIC))
	private void onTesselateWithAO(CallbackInfo ci, @Local(name = "part") BlockStateModelPart part, @Local(name = "ao") boolean ao) {
		ScopedValue<NeoForgeMeshEmitterManager> value = BlockStateModelBufferer.EMITTER_MANAGER;
		if (value.isBound()) {
			List<BakedQuad> quads = part.getQuads(null);

			if (!quads.isEmpty()) {
				ChunkSectionLayer layer = quads.getFirst().materialInfo().layer();
				NeoForgeMeshEmitter meshEmitter = value.get().getEmitter(layer);

				meshEmitter.prepareForPart(ao);
			}
		}
	}
}
