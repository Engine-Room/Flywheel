package dev.engine_room.flywheel.lib.model;

import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.api.model.IndexSequence;

public class QuadIndexSequence implements IndexSequence {
	public static final QuadIndexSequence INSTANCE = new QuadIndexSequence();

	private QuadIndexSequence() {
	}

	@Override
	public void fill(long ptr, int count) {
		int numVertices = 4 * (count / 6);
		int baseVertex = 0;
		while (baseVertex < numVertices) {
			// triangle a
			MemoryUtil.memPutInt(ptr, baseVertex);
			MemoryUtil.memPutInt(ptr + 4, baseVertex + 1);
			MemoryUtil.memPutInt(ptr + 8, baseVertex + 2);
			// triangle b
			MemoryUtil.memPutInt(ptr + 12, baseVertex);
			MemoryUtil.memPutInt(ptr + 16, baseVertex + 2);
			MemoryUtil.memPutInt(ptr + 20, baseVertex + 3);

			baseVertex += 4;
			ptr += 24;
		}
	}
}
