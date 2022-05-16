package com.jozufozu.flywheel.core;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.core.material.MaterialShaders;
import com.jozufozu.flywheel.core.model.Mesh;
import com.jozufozu.flywheel.core.model.ModelSupplier;
import com.jozufozu.flywheel.util.Lazy;
import com.jozufozu.flywheel.util.NonNullSupplier;

import net.minecraft.client.renderer.RenderType;

public class BasicModelSupplier implements ModelSupplier {
	private static final Material DEFAULT_MATERIAL = new Material(RenderType.solid(), () -> MaterialShaders.DEFAULT_VERTEX, () -> MaterialShaders.DEFAULT_FRAGMENT);

	private Material material;
	private final Lazy<Mesh> supplier;

	public BasicModelSupplier(NonNullSupplier<Mesh> supplier) {
		this(supplier, DEFAULT_MATERIAL);
	}

	public BasicModelSupplier(NonNullSupplier<Mesh> supplier, Material material) {
		this.supplier = Lazy.of(supplier);
		this.material = material;
	}

	public BasicModelSupplier setMaterial(@NotNull Material material) {
		this.material = material;
		return this;
	}

	@Override
	public Map<Material, Mesh> get() {
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
