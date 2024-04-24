package com.jozufozu.flywheel.lib.model;

import java.util.List;

import org.joml.Vector4fc;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.model.Model;

public class SimpleModel implements Model {
	private final ImmutableList<ConfiguredMesh> meshes;
	private final Vector4fc boundingSphere;

	public SimpleModel(ImmutableList<ConfiguredMesh> meshes) {
		this.meshes = meshes;
		this.boundingSphere = ModelUtil.computeBoundingSphere(meshes);
	}

	@Override
	public List<ConfiguredMesh> meshes() {
		return meshes;
	}

	@Override
	public Vector4fc boundingSphere() {
		return boundingSphere;
	}

	@Override
	public void delete() {
        for (ConfiguredMesh mesh : meshes) {
            mesh.mesh().delete();
        }
    }
}
