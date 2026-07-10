package dev.engine_room.vanillin.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.block.BlockModelSet;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BlockModelSet.class)
public interface BlockModelSetAccessor {
	@Accessor("blockModelByStateCache")
	Map<BlockState, BlockModel> flywheel$getBlockModelByStateCache();
}
