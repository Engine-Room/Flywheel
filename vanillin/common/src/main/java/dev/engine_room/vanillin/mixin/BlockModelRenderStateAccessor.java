package dev.engine_room.vanillin.mixin;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;

@Mixin(BlockModelRenderState.class)
public interface BlockModelRenderStateAccessor {
	@Nullable
	@Accessor("specialRenderer")
	SpecialModelRenderer<?> vanillin$getSpecialRenderer();
}
