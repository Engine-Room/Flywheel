package dev.engine_room.flywheel.lib.model.baked;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.material.Material;
import net.minecraft.client.renderer.RenderType;

public interface BlockMaterialFunction {
	@Nullable
	Material apply(RenderType chunkRenderType, boolean shaded, boolean ambientOcclusion);
}
