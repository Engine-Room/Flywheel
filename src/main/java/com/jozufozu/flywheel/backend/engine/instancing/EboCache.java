package com.jozufozu.flywheel.backend.engine.instancing;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.model.IndexSequence;
import com.jozufozu.flywheel.backend.gl.GlNumericType;
import com.jozufozu.flywheel.backend.gl.buffer.Buffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferUsage;
import com.jozufozu.flywheel.lib.memory.FlwMemoryTracker;
import com.jozufozu.flywheel.lib.model.QuadIndexSequence;
import com.mojang.blaze3d.platform.GlStateManager;

import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

public class EboCache {
	private final List<Entry> quads = new ArrayList<>();
	private final Object2ReferenceMap<Key, Entry> others = new Object2ReferenceOpenHashMap<>();

	public void invalidate() {
		quads.forEach(Entry::delete);
		others.values()
				.forEach(Entry::delete);
	}

	public int get(IndexSequence indexSequence, int indexCount) {
		if (indexSequence == QuadIndexSequence.INSTANCE) {
			return getQuads(indexCount);
		} else {
			return others.computeIfAbsent(new Key(indexSequence, indexCount), Key::create).ebo;
		}
	}

	private int getQuads(int indexCount) {
		// Use an existing quad EBO if there's one big enough.
		for (Entry quadEbo : quads) {
			if (quadEbo.gpuSize >= indexCount * GlNumericType.UINT.byteWidth()) {
				return quadEbo.ebo;
			}
		}
		// If not, create a new one.
		var out = Entry.create(QuadIndexSequence.INSTANCE, indexCount);
		quads.add(out);
		return out.ebo;
	}

	private record Key(IndexSequence provider, int indexCount) {
		private Entry create() {
			return Entry.create(provider, indexCount);
		}
	}

	private record Entry(int ebo, int gpuSize) {
		@NotNull
		private static Entry create(IndexSequence provider, int indexCount) {
			int byteSize = indexCount * GlNumericType.UINT.byteWidth();
			var ebo = Buffer.IMPL.create();

			final long ptr = MemoryUtil.nmemAlloc(byteSize);
			provider.fill(ptr, indexCount);

			Buffer.IMPL.data(ebo, byteSize, ptr, GlBufferUsage.STATIC_DRAW.glEnum);
			FlwMemoryTracker._allocGPUMemory(byteSize);

			MemoryUtil.nmemFree(ptr);

			return new Entry(ebo, byteSize);
		}

		private void delete() {
			GlStateManager._glDeleteBuffers(this.ebo);
			FlwMemoryTracker._freeGPUMemory(this.gpuSize);
		}
	}
}
