package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import com.google.common.collect.ImmutableList;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class MeshEmitterManager<T extends MeshEmitter> {
	private static final ChunkSectionLayer[] CHUNK_LAYERS = ChunkSectionLayer.values();

	final Reference2ReferenceMap<ChunkSectionLayer, T> emitterMap = new Reference2ReferenceArrayMap<>();
	private final ByteBufferBuilderStack byteBufferBuilderStack = new ByteBufferBuilderStack();

	MeshEmitterManager(BiFunction<ByteBufferBuilderStack, ChunkSectionLayer, T> meshEmitterFactory) {
		for (ChunkSectionLayer chunkSectionLayer : CHUNK_LAYERS) {
			emitterMap.put(chunkSectionLayer, meshEmitterFactory.apply(byteBufferBuilderStack, chunkSectionLayer));
		}
	}

	public T getEmitter(ChunkSectionLayer chunkSectionLayer) {
		return emitterMap.get(chunkSectionLayer);
	}

	public void prepare(BlockMaterialFunction blockMaterialFunction) {
		byteBufferBuilderStack.reset();

		for (MeshEmitter emitter : emitterMap.values()) {
			emitter.prepare(blockMaterialFunction);
		}
	}

	public void prepareForBlock() {
		for (MeshEmitter emitter : emitterMap.values()) {
			emitter.prepareForBlock();
		}
	}

	public SimpleModel end() {
		ImmutableList.Builder<Model.ConfiguredMesh> meshes = ImmutableList.builder();

		for (MeshEmitter emitter : emitterMap.values()) {
			emitter.end(meshes);
		}

		return new SimpleModel(meshes.build());
	}
}
