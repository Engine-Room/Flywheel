package com.jozufozu.flywheel.vanilla.model;

import org.joml.Vector4fc;

import com.jozufozu.flywheel.api.model.IndexSequence;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.vertex.MutableVertexList;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public record RetexturedMesh(Mesh mesh, TextureAtlasSprite sprite) implements Mesh {
	@Override
	public int vertexCount() {
		return mesh.vertexCount();
	}

	@Override
	public void write(MutableVertexList vertexList) {
		mesh.write(new RetexturingVertexList(vertexList, sprite));
	}

	@Override
	public IndexSequence indexSequence() {
		return mesh.indexSequence();
	}

	@Override
	public int indexCount() {
		return mesh.indexCount();
	}

	@Override
	public Vector4fc boundingSphere() {
		return mesh.boundingSphere();
	}

	@Override
	public void delete() {

	}
}
