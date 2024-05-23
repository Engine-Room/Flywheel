package dev.engine_room.flywheel.lib.material;

import dev.engine_room.flywheel.api.material.CutoutShader;
import dev.engine_room.flywheel.api.material.DepthTest;
import dev.engine_room.flywheel.api.material.FogShader;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.MaterialShaders;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.api.material.WriteMask;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class SimpleMaterial implements Material {
	protected final MaterialShaders shaders;
	protected final FogShader fog;
	protected final CutoutShader cutout;

	protected final ResourceLocation texture;
	protected final boolean blur;
	protected final boolean mipmap;

	protected final boolean backfaceCulling;
	protected final boolean polygonOffset;
	protected final DepthTest depthTest;
	protected final Transparency transparency;
	protected final WriteMask writeMask;

	protected final boolean useOverlay;
	protected final boolean useLight;
	protected final boolean diffuse;

	protected SimpleMaterial(Builder builder) {
		shaders = builder.shaders();
		fog = builder.fog();
		cutout = builder.cutout();
		texture = builder.texture();
		blur = builder.blur();
		mipmap = builder.mipmap();
		backfaceCulling = builder.backfaceCulling();
		polygonOffset = builder.polygonOffset();
		depthTest = builder.depthTest();
		transparency = builder.transparency();
		writeMask = builder.writeMask();
		useOverlay = builder.useOverlay();
		useLight = builder.useLight();
		diffuse = builder.diffuse();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builderOf(Material material) {
		return new Builder(material);
	}

	@Override
	public MaterialShaders shaders() {
		return shaders;
	}

	@Override
	public FogShader fog() {
		return fog;
	}

	@Override
	public CutoutShader cutout() {
		return cutout;
	}

	@Override
	public ResourceLocation texture() {
		return texture;
	}

	@Override
	public boolean blur() {
		return blur;
	}

	@Override
	public boolean mipmap() {
		return mipmap;
	}

	@Override
	public boolean backfaceCulling() {
		return backfaceCulling;
	}

	@Override
	public boolean polygonOffset() {
		return polygonOffset;
	}

	@Override
	public DepthTest depthTest() {
		return depthTest;
	}

	@Override
	public Transparency transparency() {
		return transparency;
	}

	@Override
	public WriteMask writeMask() {
		return writeMask;
	}

	@Override
	public boolean useOverlay() {
		return useOverlay;
	}

	@Override
	public boolean useLight() {
		return useLight;
	}

	@Override
	public boolean diffuse() {
		return diffuse;
	}

	public static class Builder implements Material {
		protected MaterialShaders shaders;
		protected FogShader fog;
		protected CutoutShader cutout;

		protected ResourceLocation texture;
		protected boolean blur;
		protected boolean mipmap;

		protected boolean backfaceCulling;
		protected boolean polygonOffset;
		protected DepthTest depthTest;
		protected Transparency transparency;
		protected WriteMask writeMask;

		protected boolean useOverlay;
		protected boolean useLight;
		protected boolean diffuse;

		public Builder() {
			shaders = StandardMaterialShaders.DEFAULT;
			fog = FogShaders.LINEAR;
			cutout = CutoutShaders.OFF;
			texture = InventoryMenu.BLOCK_ATLAS;
			blur = false;
			mipmap = true;
			backfaceCulling = true;
			polygonOffset = false;
			depthTest = DepthTest.LEQUAL;
			transparency = Transparency.OPAQUE;
			writeMask = WriteMask.COLOR_DEPTH;
			useOverlay = true;
			useLight = true;
			diffuse = true;
		}

		public Builder(Material material) {
			copyFrom(material);
		}

		public Builder copyFrom(Material material) {
			shaders = material.shaders();
			fog = material.fog();
			cutout = material.cutout();
			texture = material.texture();
			blur = material.blur();
			mipmap = material.mipmap();
			backfaceCulling = material.backfaceCulling();
			polygonOffset = material.polygonOffset();
			depthTest = material.depthTest();
			transparency = material.transparency();
			writeMask = material.writeMask();
			useOverlay = material.useOverlay();
			useLight = material.useLight();
			diffuse = material.diffuse();
			return this;
		}

		public Builder shaders(MaterialShaders value) {
			this.shaders = value;
			return this;
		}

		public Builder fog(FogShader value) {
			this.fog = value;
			return this;
		}

		public Builder cutout(CutoutShader value) {
			this.cutout = value;
			return this;
		}

		public Builder texture(ResourceLocation value) {
			this.texture = value;
			return this;
		}

		public Builder blur(boolean value) {
			this.blur = value;
			return this;
		}

		public Builder mipmap(boolean value) {
			this.mipmap = value;
			return this;
		}

		public Builder backfaceCulling(boolean value) {
			this.backfaceCulling = value;
			return this;
		}

		public Builder polygonOffset(boolean value) {
			this.polygonOffset = value;
			return this;
		}

		public Builder depthTest(DepthTest value) {
			this.depthTest = value;
			return this;
		}

		public Builder transparency(Transparency value) {
			this.transparency = value;
			return this;
		}

		public Builder writeMask(WriteMask value) {
			this.writeMask = value;
			return this;
		}

		public Builder useOverlay(boolean value) {
			this.useOverlay = value;
			return this;
		}

		public Builder useLight(boolean value) {
			this.useLight = value;
			return this;
		}

		public Builder diffuse(boolean value) {
			this.diffuse = value;
			return this;
		}

		@Override
		public MaterialShaders shaders() {
			return shaders;
		}

		@Override
		public FogShader fog() {
			return fog;
		}

		@Override
		public CutoutShader cutout() {
			return cutout;
		}

		@Override
		public ResourceLocation texture() {
			return texture;
		}

		@Override
		public boolean blur() {
			return blur;
		}

		@Override
		public boolean mipmap() {
			return mipmap;
		}

		@Override
		public boolean backfaceCulling() {
			return backfaceCulling;
		}

		@Override
		public boolean polygonOffset() {
			return polygonOffset;
		}

		@Override
		public DepthTest depthTest() {
			return depthTest;
		}

		@Override
		public Transparency transparency() {
			return transparency;
		}

		@Override
		public WriteMask writeMask() {
			return writeMask;
		}

		@Override
		public boolean useOverlay() {
			return useOverlay;
		}

		@Override
		public boolean useLight() {
			return useLight;
		}

		@Override
		public boolean diffuse() {
			return diffuse;
		}

		public SimpleMaterial build() {
			return new SimpleMaterial(this);
		}
	}
}
