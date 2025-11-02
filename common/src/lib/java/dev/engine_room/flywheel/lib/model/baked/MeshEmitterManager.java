package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;

import dev.engine_room.flywheel.api.material.Material;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import net.minecraft.client.renderer.RenderType;

class MeshEmitterManager<T extends MeshEmitter> {
	private static final RenderType[] CHUNK_LAYERS = RenderType.chunkBufferLayers().toArray(RenderType[]::new);

	final Reference2ReferenceMap<RenderType, T> emitterMap = new Reference2ReferenceArrayMap<>();

	private ModelBuilderResultConsumer resultConsumer;

	private final BufferBuilderStack bufferBuilderStack = new BufferBuilderStack();

	MeshEmitterManager(BiFunction<BufferBuilderStack, RenderType, T> meshEmitterFactory) {
		for (RenderType renderType : CHUNK_LAYERS) {
			T emitter = meshEmitterFactory.apply(bufferBuilderStack, renderType);
			emitterMap.put(renderType, emitter);
		}
	}

	public T getEmitter(RenderType renderType) {
		return emitterMap.get(renderType);
	}

	public void prepare(ModelBuilderResultConsumer resultConsumer) {
		this.resultConsumer = resultConsumer;
		bufferBuilderStack.reset();

		for (MeshEmitter emitter : emitterMap.values()) {
			emitter.prepare(resultConsumer);
		}
	}

	public void prepareForBlock() {
		for (MeshEmitter emitter : emitterMap.values()) {
			emitter.prepareForBlock();
		}
	}

	public void end() {
		resultConsumer = null;
		bufferBuilderStack.reset();

		for (MeshEmitter emitter : emitterMap.values()) {
			emitter.end();
		}
	}

	@Nullable
	public BufferBuilder getBuffer(RenderType renderType, boolean shade, boolean ao) {
		Material key = resultConsumer.createKey(renderType, shade, ao);
		if (key != null) {
			return emitterMap.get(renderType).getBuffer(key);
		} else {
			return null;
		}
	}
}
