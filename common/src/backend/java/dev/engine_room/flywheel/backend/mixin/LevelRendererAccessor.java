package dev.engine_room.flywheel.backend.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
	@Accessor("ticks")
	int flywheel$getTicks();

	@Accessor("levelRenderState")
	LevelRenderState flywheel$getLevelRenderState();
}
