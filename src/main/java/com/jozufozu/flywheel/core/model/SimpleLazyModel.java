package com.jozufozu.flywheel.core.model;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.util.Lazy;
import com.jozufozu.flywheel.util.NonNullSupplier;

public class SimpleLazyModel implements Model {
	private final Lazy<Mesh> supplier;
	private Material material;

	public SimpleLazyModel(NonNullSupplier<Mesh> supplier, Material material) {
		this.supplier = Lazy.of(supplier);
		this.material = material;
	}

	public SimpleLazyModel setMaterial(@NotNull Material material) {
		this.material = material;
		return this;
	}

	@Override
	public Map<Material, Mesh> getMeshes() {
		return ImmutableMap.of(material, supplier.get());
	}

	public int getVertexCount() {
		return supplier.map(Mesh::getVertexCount)
				.orElse(0);
	}

	@Override
	public String toString() {
		return "BasicModelSupplier{" + supplier.map(Mesh::name)
				.orElse("Uninitialized") + '}';
	}
}
