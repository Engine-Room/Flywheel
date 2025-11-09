package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.BufferBuilder;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import net.minecraft.client.renderer.RenderType;

class MeshEmitterManager<T extends MeshEmitter> {
	private static final RenderType[] CHUNK_LAYERS = RenderType.chunkBufferLayers().toArray(RenderType[]::new);

	private final Reference2ReferenceMap<RenderType, T> emitterMap = new Reference2ReferenceArrayMap<>();

	@UnknownNullability
	private BlockMaterialFunction blockMaterialFunction;

	MeshEmitterManager(Function<RenderType, T> meshEmitterFactory) {
		for (RenderType renderType : CHUNK_LAYERS) {
			emitterMap.put(renderType, meshEmitterFactory.apply(renderType));
		}
	}

	public T getEmitter(RenderType renderType) {
		return emitterMap.get(renderType);
	}

	public void prepare(BlockMaterialFunction blockMaterialFunction) {
		this.blockMaterialFunction = blockMaterialFunction;

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
		blockMaterialFunction = null;

		ImmutableList.Builder<Model.ConfiguredMesh> meshes = ImmutableList.builder();

		for (MeshEmitter emitter : emitterMap.values()) {
			emitter.end(meshes);
		}

		return new SimpleModel(meshes.build());
	}

	@Nullable
	public BufferBuilder getBuffer(RenderType renderType, boolean shade, boolean ao) {
		Material key = blockMaterialFunction.apply(renderType, shade, ao);
		if (key != null) {
			return emitterMap.get(renderType).getBuffer(key);
		} else {
			return null;
		}
	}
}
