package com.jozufozu.flywheel.core;

import javax.annotation.Nonnull;

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

	public BasicModelSupplier setRenderType(@Nonnull RenderType renderType) {
		this.renderType = renderType;
		return this;
	}

	@Override
	public Mesh get() {
		return supplier.get();
	}

	@Nonnull
	public RenderType getRenderType() {
		return renderType;
	}

	public int getVertexCount() {
		return supplier.map(Mesh::vertexCount)
				.orElse(0);
	}

	@Override
	public String toString() {
		return "ModelSupplier{" + supplier.map(Mesh::name)
				.orElse("Uninitialized") + '}';
	}
}
