package dev.engine_room.flywheel.lib.model;

import org.jetbrains.annotations.UnknownNullability;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.model.IndexSequence;
import dev.engine_room.flywheel.api.model.Mesh;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.vertex.MutableVertexList;
import dev.engine_room.flywheel.api.vertex.VertexList;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.material.StandardMaterialShaders;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;
import dev.engine_room.flywheel.lib.vertex.FullVertexView;
import dev.engine_room.flywheel.lib.vertex.VertexView;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;

public final class LineModelBuilder {
	private static final Material MATERIAL = SimpleMaterial.builder()
			.shaders(StandardMaterialShaders.LINE)
			.backfaceCulling(false)
			.cardinalLightingMode(CardinalLightingMode.OFF)
			.build();

	@UnknownNullability
	private VertexView vertexView;
	@UnknownNullability
	private MemoryBlock data;
	private int vertexCount = 0;

	public LineModelBuilder() {
	}

	public LineModelBuilder(int initialSegmentCount) {
		ensureCapacity(initialSegmentCount);
	}

	public void ensureCapacity(int segmentCount) {
		if (segmentCount < 0) {
			throw new IllegalArgumentException("Segment count must be greater than or equal to 0");
		} else if (segmentCount == 0) {
			return;
		}

		if (data == null) {
			vertexView = new FullVertexView();
			data = MemoryBlock.mallocTracked(segmentCount * 4 * vertexView.stride());
			vertexView.ptr(data.ptr());
			vertexCount = 0;
		} else {
			long requiredCapacity = (vertexCount + segmentCount * 4) * vertexView.stride();

			if (requiredCapacity > data.size()) {
				data = data.realloc(requiredCapacity);
				vertexView.ptr(data.ptr());
			}
		}
	}

	public LineModelBuilder line(float x1, float y1, float z1, float x2, float y2, float z2) {
		ensureCapacity(1);

		// We'll use the normal to figure out the orientation of the line in the vertex shader.
		float dx = x2 - x1;
		float dy = y2 - y1;
		float dz = z2 - z1;
		float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
		float normalX = dx / length;
		float normalY = dy / length;
		float normalZ = dz / length;

		for (int i = 0; i < 2; i++) {
			vertexView.x(vertexCount + i, x1);
			vertexView.y(vertexCount + i, y1);
			vertexView.z(vertexCount + i, z1);

			vertexView.x(vertexCount + 2 + i, x2);
			vertexView.y(vertexCount + 2 + i, y2);
			vertexView.z(vertexCount + 2 + i, z2);
		}

		for (int i = 0; i < 4; i++) {
			vertexView.r(vertexCount + i, 1);
			vertexView.g(vertexCount + i, 1);
			vertexView.b(vertexCount + i, 1);
			vertexView.a(vertexCount + i, 1);
			vertexView.u(vertexCount + i, 0);
			vertexView.v(vertexCount + i, 0);
			vertexView.overlay(vertexCount + i, OverlayTexture.NO_OVERLAY);
			vertexView.light(vertexCount + i, LightTexture.FULL_BRIGHT);
			vertexView.normalX(vertexCount + i, normalX);
			vertexView.normalY(vertexCount + i, normalY);
			vertexView.normalZ(vertexCount + i, normalZ);
		}

		vertexCount += 4;
		return this;
	}

	public Model build() {
		if (vertexCount == 0) {
			return EmptyModel.INSTANCE;
		}

		long requiredCapacity = vertexCount * vertexView.stride();

		if (data.size() > requiredCapacity) {
			data = data.realloc(requiredCapacity);
			vertexView.ptr(data.ptr());
		}

		vertexView.nativeMemoryOwner(data);
		vertexView.vertexCount(vertexCount);

		var boundingSphere = ModelUtil.computeBoundingSphere(vertexView);
		boundingSphere.w += 0.1f; // make the bounding sphere a little bigger to account for line width
		var mesh = new LineMesh(vertexView, boundingSphere);
		var model = new SingleMeshModel(mesh, MATERIAL);

		vertexView = null;
		data = null;
		vertexCount = 0;

		return model;
	}

	private static class LineMesh implements Mesh {
		private static final IndexSequence INDEX_SEQUENCE = (ptr, count) -> {
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
		private final VertexList vertexList;
		private final Vector4fc boundingSphere;

		public LineMesh(VertexList vertexList, Vector4fc boundingSphere) {
			this.vertexList = vertexList;
			this.boundingSphere = boundingSphere;
		}

		@Override
		public int vertexCount() {
			return vertexList.vertexCount();
		}

		@Override
		public void write(MutableVertexList vertexList) {
			this.vertexList.writeAll(vertexList);
		}

		@Override
		public IndexSequence indexSequence() {
			return INDEX_SEQUENCE;
		}

		@Override
		public int indexCount() {
			return vertexCount() / 2 * 3;
		}

		@Override
		public Vector4fc boundingSphere() {
			return boundingSphere;
		}
	}
}
