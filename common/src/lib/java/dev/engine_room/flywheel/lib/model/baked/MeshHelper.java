package dev.engine_room.flywheel.lib.model.baked;

import java.nio.ByteBuffer;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.vertex.BufferBuilder;

import dev.engine_room.flywheel.lib.memory.MemoryBlock;
import dev.engine_room.flywheel.lib.model.SimpleQuadMesh;
import dev.engine_room.flywheel.lib.vertex.NoOverlayVertexView;
import dev.engine_room.flywheel.lib.vertex.VertexView;

final class MeshHelper {
	private MeshHelper() {
	}

	public static SimpleQuadMesh blockVerticesToMesh(BufferBuilder.RenderedBuffer buffer, @Nullable String meshDescriptor) {
		BufferBuilder.DrawState drawState = buffer.drawState();
		int vertexCount = drawState.vertexCount();
		long srcStride = drawState.format().getVertexSize();

		VertexView vertexView = new NoOverlayVertexView();
		long dstStride = vertexView.stride();

		ByteBuffer src = buffer.vertexBuffer();
		MemoryBlock dst = MemoryBlock.mallocTracked((long) vertexCount * dstStride);
		long srcPtr = MemoryUtil.memAddress(src);
		long dstPtr = dst.ptr();
		// The first 31 bytes of each vertex in a block vertex buffer are guaranteed to contain the same data in the
		// same order regardless of whether the format is extended by mods like Iris or OptiFine. Copy these bytes and
		// ignore the rest.
		long bytesToCopy = Math.min(dstStride, 31);

		for (int i = 0; i < vertexCount; i++) {
			// It is safe to copy bytes directly since the NoOverlayVertexView uses the same memory layout as the first
			// 31 bytes of the block vertex format, vanilla or otherwise.
			MemoryUtil.memCopy(srcPtr + srcStride * i, dstPtr + dstStride * i, bytesToCopy);
		}

		vertexView.ptr(dstPtr);
		vertexView.vertexCount(vertexCount);
		vertexView.nativeMemoryOwner(dst);

		return new SimpleQuadMesh(vertexView, meshDescriptor);
	}
}
