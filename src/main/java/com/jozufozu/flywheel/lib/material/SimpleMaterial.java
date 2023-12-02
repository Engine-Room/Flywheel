package com.jozufozu.flywheel.lib.material;

import com.jozufozu.flywheel.api.material.CutoutShader;
import com.jozufozu.flywheel.api.material.FogShader;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.material.MaterialShaders;
import com.jozufozu.flywheel.api.material.MaterialVertexTransformer;
import com.jozufozu.flywheel.api.material.Transparency;
import com.jozufozu.flywheel.api.material.WriteMask;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class SimpleMaterial implements Material {
	protected final MaterialShaders shaders;
	protected final RenderType fallbackRenderType;
	protected final MaterialVertexTransformer vertexTransformer;

	protected final ResourceLocation baseTexture;
	protected final boolean diffuse;
	protected final boolean lighting;
	protected final boolean blur;
	protected final boolean backfaceCull;
	protected final boolean polygonOffset;
	protected final boolean mip;
	protected final FogShader fog;
	protected final Transparency transparency;
	protected final CutoutShader cutout;
	protected final WriteMask writeMask;

	protected SimpleMaterial(Builder builder) {
		this.shaders = builder.shaders;
		this.fallbackRenderType = builder.fallbackRenderType;
		this.vertexTransformer = builder.vertexTransformer;
		this.baseTexture = builder.baseTexture;
		this.diffuse = builder.diffuse;
		this.lighting = builder.lighting;
		this.blur = builder.blur;
		this.backfaceCull = builder.backfaceCull;
		this.polygonOffset = builder.polygonOffset;
		this.mip = builder.mip;
		this.fog = builder.fog;
		this.transparency = builder.transparency;
		this.cutout = builder.cutout;
		this.writeMask = builder.writeMask;
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public MaterialShaders shaders() {
		return shaders;
	}

	@Override
	public RenderType getFallbackRenderType() {
		return fallbackRenderType;
	}

	@Override
	public MaterialVertexTransformer getVertexTransformer() {
		return vertexTransformer;
	}

	@Override
	public ResourceLocation baseTexture() {
		return baseTexture;
	}

	@Override
	public boolean diffuse() {
		return diffuse;
	}

	@Override
	public boolean lighting() {
		return lighting;
	}

	@Override
	public boolean blur() {
		return blur;
	}

	@Override
	public boolean backfaceCull() {
		return backfaceCull;
	}

	@Override
	public boolean polygonOffset() {
		return polygonOffset;
	}

	@Override
	public boolean mip() {
		return mip;
	}

	@Override
	public FogShader fog() {
		return fog;
	}

	@Override
	public Transparency transparency() {
		return transparency;
	}

	@Override
	public CutoutShader cutout() {
		return cutout;
	}

	@Override
	public WriteMask writeMask() {
		return WriteMask.BOTH;
	}

	public static class Builder {
		protected RenderType fallbackRenderType = RenderType.solid();
		protected MaterialVertexTransformer vertexTransformer = (vertexList, level) -> {};
		protected MaterialShaders shaders = StandardMaterialShaders.DEFAULT;
		protected ResourceLocation baseTexture = InventoryMenu.BLOCK_ATLAS;
		protected boolean diffuse = true;
		protected boolean lighting = true;
		protected boolean blur = false;
		protected boolean backfaceCull = true;
		protected boolean polygonOffset = false;
		protected boolean mip = true;
		protected FogShader fog = StandardMaterialShaders.LINEAR;
		protected Transparency transparency = Transparency.OPAQUE;
		protected CutoutShader cutout = StandardMaterialShaders.OFF;
		protected WriteMask writeMask = WriteMask.BOTH;

		public Builder() {
		}

		public Builder fallbackRenderType(RenderType type) {
			this.fallbackRenderType = type;
			return this;
		}

		public Builder vertexTransformer(MaterialVertexTransformer vertexTransformer) {
			this.vertexTransformer = vertexTransformer;
			return this;
		}

		public Builder shaders(MaterialShaders value) {
			this.shaders = value;
			return this;
		}

		public Builder baseTexture(ResourceLocation value) {
			this.baseTexture = value;
			return this;
		}

		public Builder diffuse(boolean value) {
			this.diffuse = value;
			return this;
		}

		public Builder lighting(boolean value) {
			this.lighting = value;
			return this;
		}

		public Builder blur(boolean value) {
			this.blur = value;
			return this;
		}

		public Builder backfaceCull(boolean value) {
			this.backfaceCull = value;
			return this;
		}

		public Builder polygonOffset(boolean value) {
			this.polygonOffset = value;
			return this;
		}

		public Builder mip(boolean value) {
			this.mip = value;
			return this;
		}

		public Builder fog(FogShader value) {
			this.fog = value;
			return this;
		}

		public Builder transparency(Transparency value) {
			this.transparency = value;
			return this;
		}

		public Builder cutout(CutoutShader value) {
			this.cutout = value;
			return this;
		}

		public Builder writeMask(WriteMask value) {
			this.writeMask = value;
			return this;
		}

		public SimpleMaterial build() {
			return new SimpleMaterial(this);
		}
	}
}
