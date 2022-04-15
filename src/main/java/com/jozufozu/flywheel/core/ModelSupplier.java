package com.jozufozu.flywheel.core;

import javax.annotation.Nonnull;

import com.jozufozu.flywheel.core.model.Model;
import com.jozufozu.flywheel.util.Lazy;
import com.jozufozu.flywheel.util.NonNullSupplier;

import net.minecraft.client.renderer.RenderType;

public class ModelSupplier extends Lazy<Model> {

	private RenderType renderType;

	public ModelSupplier(NonNullSupplier<Model> supplier) {
		this(supplier, RenderType.solid());
	}

	public ModelSupplier(NonNullSupplier<Model> supplier, RenderType renderType) {
		super(supplier);
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
	public RenderType getRenderType() {
		return renderType;
	}

	public int getVertexCount() {
		return map(Model::vertexCount).orElse(0);
	}

	@Override
	public String toString() {
		return "ModelSupplier{" + map(Model::name).orElse("Uninitialized") + '}';
	}
}
