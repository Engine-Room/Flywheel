package com.jozufozu.flywheel.core;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.core.model.Mesh;
import com.jozufozu.flywheel.core.model.ModelSupplier;
import com.jozufozu.flywheel.util.Lazy;
import com.jozufozu.flywheel.util.NonNullSupplier;

import net.minecraft.client.renderer.RenderType;

public class BasicModelSupplier implements ModelSupplier {

	private RenderType renderType;
	private final Lazy<Mesh> supplier;

	public BasicModelSupplier(NonNullSupplier<Mesh> supplier) {
		this(supplier, RenderType.solid());
	}

	public BasicModelSupplier(NonNullSupplier<Mesh> supplier, RenderType renderType) {
		this.supplier = Lazy.of(supplier);
		this.renderType = renderType;
	}

	public BasicModelSupplier setCutout() {
		return setRenderType(RenderType.cutoutMipped());
	}

	public BasicModelSupplier setRenderType(@NotNull RenderType renderType) {
		this.renderType = renderType;
		return this;
	}

	@Override
	public Map<RenderType, Mesh> get() {
		return ImmutableMap.of(renderType, supplier.get());
	}

	public int getVertexCount() {
		return supplier.map(Mesh::getVertexCount)
				.orElse(0);
	}

	@Override
	public String toString() {
		return "ModelSupplier{" + supplier.map(Mesh::name)
				.orElse("Uninitialized") + '}';
	}
}
