package dev.engine_room.flywheel.impl.mixin.neoforge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import dev.engine_room.flywheel.lib.model.baked.BlockStateModelBufferer;
import dev.engine_room.flywheel.lib.model.baked.NeoForgeMeshEmitter;
import dev.engine_room.flywheel.lib.model.baked.NeoForgeMeshEmitterManager;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ModelBlockRenderer.class)
abstract class ModelBlockRendererMixin {
	@WrapOperation(method = "tesselateAmbientOcclusion", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;putQuadWithTint(Lnet/minecraft/client/renderer/block/BlockQuadOutput;FFFLnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/resources/model/geometry/BakedQuad;)V"))
	private void onTesselateWithAO(ModelBlockRenderer instance, BlockQuadOutput output, float x, float y, float z, BlockAndTintGetter level, BlockState state, BlockPos pos, BakedQuad quad, Operation<Void> original, @Local(name = "ao") boolean ao) {
		ScopedValue<NeoForgeMeshEmitterManager> value = BlockStateModelBufferer.EMITTER_MANAGER;
		if (value.isBound()) {
			ChunkSectionLayer layer = quad.materialInfo().layer();
			NeoForgeMeshEmitter meshEmitter = value.get().getEmitter(layer);

			meshEmitter.prepareForPart(ao);
		}

		original.call(instance, output, x, y, z, level, state, pos, quad);
	}
}
