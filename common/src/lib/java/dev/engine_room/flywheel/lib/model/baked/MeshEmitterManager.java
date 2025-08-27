package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import com.mojang.blaze3d.vertex.BufferBuilder;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.client.renderer.RenderType;

class MeshEmitterManager<T extends MeshEmitter> {
	private static final RenderType[] CHUNK_LAYERS = RenderType.chunkBufferLayers().toArray(RenderType[]::new);

	final Reference2ReferenceMap<RenderType, T> emitterMap = new Reference2ReferenceLinkedOpenHashMap<>();
	private final ReferenceArrayList<BufferBuilder> bufferBuilders = new ReferenceArrayList<>();

	@UnknownNullability
	private ResultConsumer<?> resultConsumer;
	private int nextBufferBuilderIndex;

	MeshEmitterManager(BiFunction<Supplier<BufferBuilder>, RenderType, T> meshEmitterFactory) {
		for (RenderType renderType : CHUNK_LAYERS) {
			T emitter = meshEmitterFactory.apply(this::getOrCreateBufferBuilder, renderType);
			emitterMap.put(renderType, emitter);
		}
	}

	public void prepare(ResultConsumer<?> resultConsumer) {
		this.resultConsumer = resultConsumer;
		nextBufferBuilderIndex = 0;

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
		nextBufferBuilderIndex = 0;

		for (MeshEmitter emitter : emitterMap.values()) {
			emitter.end();
		}
	}

	@Nullable
	public BufferBuilder getBuffer(RenderType renderType, boolean shade, boolean ao) {
		Object key = resultConsumer.createKey(renderType, shade, ao);
		if (key != null) {
			return emitterMap.get(renderType).getBuffer(key);
		} else {
			return null;
		}
	}

	private BufferBuilder getOrCreateBufferBuilder() {
		BufferBuilder bufferBuilder;
		if (nextBufferBuilderIndex < bufferBuilders.size()) {
			bufferBuilder = bufferBuilders.get(nextBufferBuilderIndex);
		} else {
			bufferBuilder = new BufferBuilder(0);
			bufferBuilders.add(bufferBuilder);
		}
		nextBufferBuilderIndex++;
		return bufferBuilder;
	}
}
