package com.jozufozu.flywheel.mixin;

import java.util.SortedSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.server.level.BlockDestructionProgress;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
	@Accessor("destructionProgress")
	Long2ObjectMap<SortedSet<BlockDestructionProgress>> flywheel$getDestructionProgress();
}
