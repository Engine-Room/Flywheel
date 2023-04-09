package com.jozufozu.flywheel.lib.model;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.util.Lazy;
import com.jozufozu.flywheel.util.NonNullSupplier;

public class SimpleLazyModel implements Model {
	private final Lazy<Mesh> supplier;
	private final Material material;

	public SimpleLazyModel(NonNullSupplier<Mesh> supplier, Material material) {
		this.supplier = Lazy.of(supplier);
		this.material = material;
	}

	@Override
	public Map<Material, Mesh> getMeshes() {
		return ImmutableMap.of(material, supplier.get());
	}

	@Override
	public void delete() {
		supplier.ifPresent(Mesh::delete);
	}

	public int getVertexCount() {
		return supplier.map(Mesh::getVertexCount)
				.orElse(0);
	}

	@Override
	public String toString() {
		return "SimpleLazyModel{" + supplier.map(Mesh::name)
				.orElse("Uninitialized") + '}';
	}
}
