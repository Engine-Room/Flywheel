package dev.engine_room.flywheel.lib.model.baked;

import org.jspecify.annotations.Nullable;

import dev.engine_room.flywheel.api.material.Material;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public interface BlockMaterialFunction {
	@Nullable
	Material apply(ChunkSectionLayer chunkSectionLayer, boolean shaded, boolean ambientOcclusion);
}
