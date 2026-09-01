package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

@Internal
public class NeoForgeMeshEmitterManager extends MeshEmitterManager<NeoForgeMeshEmitter> implements Function<ChunkSectionLayer, VertexConsumer> {
	NeoForgeMeshEmitterManager() {
		super(NeoForgeMeshEmitter::new);
	}

	public void prepareForModel(boolean ao) {
		for (NeoForgeMeshEmitter meshEmitter : emitterMap.values()) {
			meshEmitter.prepareForPart(ao);
		}
	}

	@Override
	public VertexConsumer apply(ChunkSectionLayer chunkSectionLayer) {
		return getEmitter(chunkSectionLayer);
	}
}
