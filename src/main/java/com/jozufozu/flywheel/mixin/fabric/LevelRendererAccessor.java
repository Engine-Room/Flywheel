package com.jozufozu.flywheel.mixin.fabric;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LevelRenderer;

import net.minecraft.server.level.BlockDestructionProgress;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.SortedSet;

@Environment(EnvType.CLIENT)
@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
	@Accessor("destructionProgress")
	Long2ObjectMap<SortedSet<BlockDestructionProgress>> getDestructionProgress();
}
