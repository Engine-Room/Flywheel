package com.jozufozu.flywheel.lib.model;

import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.model.Model;

public class SimpleLazyModel implements Model {
	private final Supplier<@NotNull Mesh> meshSupplier;
	private final Material material;

	@Nullable
	private Mesh mesh;
	@Nullable
	private Map<Material, Mesh> meshMap;

	public SimpleLazyModel(Supplier<@NotNull Mesh> meshSupplier, Material material) {
		this.meshSupplier = meshSupplier;
		this.material = material;
	}

	@Override
	public Map<Material, Mesh> getMeshes() {
		if (mesh == null) {
			mesh = meshSupplier.get();
			meshMap = ImmutableMap.of(material, mesh);
		}

		return meshMap;
	}

	@Override
	public void delete() {
		if (mesh != null) {
			mesh.delete();
		}
	}

	@Override
	public String toString() {
		String name = mesh != null ? mesh.name() : "Uninitialized";
		return "SimpleLazyModel{" + name + '}';
	}
}
