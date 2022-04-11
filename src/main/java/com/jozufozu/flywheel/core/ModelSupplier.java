package com.jozufozu.flywheel.core;

import javax.annotation.Nonnull;

import com.jozufozu.flywheel.core.model.Model;
import com.jozufozu.flywheel.util.Lazy;
import com.jozufozu.flywheel.util.NonNullSupplier;

import net.minecraft.client.renderer.RenderType;

public class ModelSupplier {

	private final Lazy<Model> supplier;

	private RenderType renderType;

	public ModelSupplier(NonNullSupplier<Model> supplier) {
		this(supplier, RenderType.solid());
	}

	public ModelSupplier(NonNullSupplier<Model> supplier, RenderType renderType) {
		this.supplier = Lazy.of(supplier);
		this.renderType = renderType;
	}

	public ModelSupplier setCutout() {
		return setRenderType(RenderType.cutoutMipped());
	}

	public ModelSupplier setRenderType(@Nonnull RenderType renderType) {
		this.renderType = renderType;
		return this;
	}

	@Nonnull
	public Model get() {
		return supplier.get();
	}

	@Nonnull
	public RenderType getRenderType() {
		return renderType;
	}
}
