package dev.engine_room.vanillin.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(ChestRenderer.class)
public interface ChestRendererAccessor {
	@Invoker("getChestMaterial")
	static ChestRenderState.ChestMaterialType flywheel$getChestMaterial(BlockEntity blockEntity, boolean xmasTextures) {
		throw new AssertionError();
	}
}
