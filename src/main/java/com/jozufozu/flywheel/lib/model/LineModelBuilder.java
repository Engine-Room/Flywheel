package com.jozufozu.flywheel.lib.model;

import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.IndexSequence;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.api.vertex.VertexView;
import com.jozufozu.flywheel.lib.material.SimpleMaterial;
import com.jozufozu.flywheel.lib.material.StandardMaterialShaders;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.jozufozu.flywheel.lib.vertex.FullVertexView;

import net.minecraft.client.renderer.LightTexture;

public class LineModelBuilder {
	public static final Material MATERIAL = SimpleMaterial.builder()
			.shaders(StandardMaterialShaders.LINE)
			.backfaceCulling(false)
			.diffuse(false)
			.build();

	private final VertexView vertices;
	private MemoryBlock block;
	private int vertexCount = 0;

	private LineModelBuilder(int segmentCount) {
		this.vertices = new FullVertexView();
		this.block = MemoryBlock.malloc(segmentCount * 4 * FullVertexView.STRIDE);
		vertices.ptr(block.ptr());
	}

	public static LineModelBuilder withCapacity(int segmentCount) {
		return new LineModelBuilder(segmentCount);
	}

	public LineModelBuilder line(float x1, float y1, float z1, float x2, float y2, float z2) {
		ensureCapacity(vertexCount + 4);

		// We'll use the normal to figure out the orientation of the line in the vertex shader.
		float dx = x2 - x1;
		float dy = y2 - y1;
		float dz = z2 - z1;
		float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
		float normalX = dx / length;
		float normalY = dy / length;
		float normalZ = dz / length;

		for (int i = 0; i < 2; i++) {
			vertices.x(vertexCount + i, x1);
			vertices.y(vertexCount + i, y1);
			vertices.z(vertexCount + i, z1);

			vertices.x(vertexCount + 2 + i, x2);
			vertices.y(vertexCount + 2 + i, y2);
			vertices.z(vertexCount + 2 + i, z2);
		}

		for (int i = 0; i < 4; i++) {
			vertices.r(vertexCount + i, 1);
			vertices.g(vertexCount + i, 1);
			vertices.b(vertexCount + i, 1);
			vertices.a(vertexCount + i, 1);
			vertices.u(vertexCount + i, 0);
			vertices.v(vertexCount + i, 0);
			vertices.light(vertexCount + i, LightTexture.FULL_BRIGHT);
			vertices.normalX(vertexCount + i, normalX);
			vertices.normalY(vertexCount + i, normalY);
			vertices.normalZ(vertexCount + i, normalZ);
		}

		vertexCount += 4;

		return this;
	}

	public Model build() {
		vertices.vertexCount(vertexCount);

		var boundingSphere = ModelUtil.computeBoundingSphere(vertices);
		boundingSphere.w += 0.1f; // make the bounding sphere a little bigger to account for line width

		var mesh = new LineMesh(vertexCount, vertices, block, boundingSphere);

		return new SingleMeshModel(mesh, MATERIAL);
	}

	private void ensureCapacity(int vertexCount) {
		if (vertexCount * FullVertexView.STRIDE > block.size()) {
			this.block = block.realloc(vertexCount * FullVertexView.STRIDE);
			vertices.ptr(block.ptr());
		}
	}

	public static class LineMesh implements Mesh {
		public static final IndexSequence INDEX_SEQUENCE = (ptr, count) -> {
			int numVertices = 2 * count / 3;
			int baseVertex = 0;
			while (baseVertex < numVertices) {
				// triangle a
				MemoryUtil.memPutInt(ptr, baseVertex);
				MemoryUtil.memPutInt(ptr + 4, baseVertex + 1);
				MemoryUtil.memPutInt(ptr + 8, baseVertex + 2);
				// triangle b
				MemoryUtil.memPutInt(ptr + 12, baseVertex + 3);
				MemoryUtil.memPutInt(ptr + 16, baseVertex + 2);
				MemoryUtil.memPutInt(ptr + 20, baseVertex + 1);

				baseVertex += 4;
				ptr += 24;
			}
		};
		private final int vertexCount;
		private final VertexView vertexView;
		private final MemoryBlock data;
		private final Vector4f boundingSphere;

		public LineMesh(int vertexCount, VertexView vertexView, MemoryBlock data, Vector4f boundingSphere) {
			this.vertexCount = vertexCount;
			this.vertexView = vertexView;
			this.data = data;
			this.boundingSphere = boundingSphere;
		}

		@Override
		public int vertexCount() {
			return vertexCount;
		}

		@Override
		public void write(MutableVertexList vertexList) {
			vertexView.writeAll(vertexList);
		}

		@Override
		public IndexSequence indexSequence() {
			return INDEX_SEQUENCE;
		}

		@Override
		public int indexCount() {
			return vertexCount / 2 * 3;
		}

		@Override
		public Vector4fc boundingSphere() {
			return boundingSphere;
		}

		@Override
		public void delete() {
			data.free();
		}
	}
}
