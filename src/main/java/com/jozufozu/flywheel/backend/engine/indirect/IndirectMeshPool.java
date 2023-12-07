package com.jozufozu.flywheel.backend.engine.indirect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.backend.InternalLayout;
import com.jozufozu.flywheel.gl.GlNumericType;
import com.jozufozu.flywheel.gl.array.GlVertexArray;
import com.jozufozu.flywheel.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.jozufozu.flywheel.lib.model.QuadIndexSequence;

public class IndirectMeshPool {
	private final Map<Mesh, BufferedMesh> meshes = new HashMap<>();
	private final List<BufferedMesh> meshList = new ArrayList<>();

	private final GlVertexArray vertexArray;
	private final GlBuffer vbo;
	private final GlBuffer ebo;

	private boolean dirty;

	/**
	 * Create a new mesh pool.
	 */
	public IndirectMeshPool() {
		vbo = new GlBuffer();
		ebo = new GlBuffer();
		vertexArray = GlVertexArray.create();

		vertexArray.setElementBuffer(ebo.handle());
		vertexArray.bindVertexBuffer(0, vbo.handle(), 0, InternalLayout.LAYOUT.getStride());
		vertexArray.bindAttributes(0, 0, InternalLayout.LAYOUT.attributes());
	}

	/**
	 * Allocate a model in the arena.
	 *
	 * @param mesh The model to allocate.
	 * @return A handle to the allocated model.
	 */
	public BufferedMesh alloc(Mesh mesh) {
		return meshes.computeIfAbsent(mesh, m -> {
			BufferedMesh bufferedModel = new BufferedMesh(m);
			meshList.add(bufferedModel);

			dirty = true;
			return bufferedModel;
		});
	}

	@Nullable
	public BufferedMesh get(Mesh mesh) {
		return meshes.get(mesh);
	}

	public void flush(StagingBuffer stagingBuffer) {
		if (dirty) {
			// TODO: use the staging buffer and be smarter about allocation in general.
			uploadAll(stagingBuffer);
			dirty = false;
		}
	}

	private void uploadAll(StagingBuffer stagingBuffer) {
		long neededSize = 0;
		int maxQuadIndexCount = 0;
		int nonQuadIndexCount = 0;
		for (BufferedMesh mesh : meshList) {
			neededSize += mesh.size();

			if (mesh.mesh.indexSequence() == QuadIndexSequence.INSTANCE) {
				maxQuadIndexCount = Math.max(maxQuadIndexCount, mesh.mesh.indexCount());
			} else {
				nonQuadIndexCount += mesh.mesh.indexCount();
			}
		}

		final long totalIndexCount = maxQuadIndexCount + nonQuadIndexCount;

		final var vertexBlock = MemoryBlock.malloc(neededSize);
		final var indexBlock = MemoryBlock.malloc(totalIndexCount * GlNumericType.UINT.byteWidth());

		final long vertexPtr = vertexBlock.ptr();
		final long indexPtr = indexBlock.ptr();

		var target = InternalLayout.createVertexList();

		int byteIndex = 0;
		int baseVertex = 0;
		int firstIndex = maxQuadIndexCount;
		for (BufferedMesh mesh : meshList) {
			mesh.byteIndex = byteIndex;
			mesh.baseVertex = baseVertex;

			target.ptr(vertexPtr + mesh.byteIndex);
			mesh.mesh.write(target);

			byteIndex += mesh.size();
			baseVertex += mesh.mesh.vertexCount();

			var indexFiller = mesh.mesh.indexSequence();
			if (indexFiller == QuadIndexSequence.INSTANCE) {
				mesh.firstIndex = 0;
			} else {
				var indexCount = mesh.mesh.indexCount();
				mesh.firstIndex = firstIndex;
				indexFiller.fill(indexPtr + (long) firstIndex * GlNumericType.UINT.byteWidth(), indexCount);

				firstIndex += indexCount;
			}
		}

		if (maxQuadIndexCount > 0) {
			QuadIndexSequence.INSTANCE.fill(indexPtr, maxQuadIndexCount);
		}

		vbo.upload(vertexBlock);
		ebo.upload(indexBlock);

		vertexBlock.free();
		indexBlock.free();
	}

	public void bindForDraw() {
		vertexArray.bindForDraw();
	}

	public void delete() {
		vertexArray.delete();
		vbo.delete();
		ebo.delete();
		meshes.clear();
		meshList.clear();
	}

	public static class BufferedMesh {
		private final Mesh mesh;
		private long byteIndex;
		private int baseVertex;
		private int firstIndex;

		private BufferedMesh(Mesh mesh) {
			this.mesh = mesh;
		}

		public int size() {
			return mesh.vertexCount() * InternalLayout.LAYOUT.getStride();
		}

		public int indexCount() {
			return mesh.indexCount();
		}

		public int baseVertex() {
			return baseVertex;
		}

		public int firstIndex() {
			return firstIndex;
		}
	}
}
