package dev.engine_room.flywheel.lib.material;

import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.CutoutShader;
import dev.engine_room.flywheel.api.material.DepthTest;
import dev.engine_room.flywheel.api.material.FogShader;
import dev.engine_room.flywheel.api.material.LightShader;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.MaterialShaders;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.api.material.WriteMask;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

public class SimpleMaterial implements Material {
	protected final MaterialShaders shaders;
	protected final FogShader fog;
	protected final CutoutShader cutout;
	protected final LightShader light;

	protected final Identifier texture;
	protected final boolean blur;
	protected final boolean mipmap;

	protected final boolean backfaceCulling;
	protected final boolean polygonOffset;
	protected final DepthTest depthTest;
	protected final Transparency transparency;
	protected final WriteMask writeMask;

	protected final boolean useOverlay;
	protected final boolean useLight;
	protected final CardinalLightingMode cardinalLightingMode;

	protected SimpleMaterial(Builder builder) {
		shaders = builder.shaders();
		fog = builder.fog();
		cutout = builder.cutout();
		light = builder.light();
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
		cardinalLightingMode = builder.cardinalLightingMode();
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
	public LightShader light() {
		return light;
	}

	@Override
	public Identifier texture() {
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
	public CardinalLightingMode cardinalLightingMode() {
		return cardinalLightingMode;
	}

	public static class Builder implements Material {
		protected MaterialShaders shaders;
		protected FogShader fog;
		protected CutoutShader cutout;
		protected LightShader light;

		protected Identifier texture;
		protected boolean blur;
		protected boolean mipmap;

		protected boolean backfaceCulling;
		protected boolean polygonOffset;
		protected DepthTest depthTest;
		protected Transparency transparency;
		protected WriteMask writeMask;

		protected boolean useOverlay;
		protected boolean useLight;
		protected CardinalLightingMode cardinalLightingMode;

		public Builder() {
			shaders = StandardMaterialShaders.DEFAULT;
			fog = FogShaders.LINEAR;
			cutout = CutoutShaders.OFF;
			light = LightShaders.SMOOTH_WHEN_EMBEDDED;
			texture = TextureAtlas.LOCATION_BLOCKS;
			blur = false;
			mipmap = true;
			backfaceCulling = true;
			polygonOffset = false;
			depthTest = DepthTest.LEQUAL;
			transparency = Transparency.OPAQUE;
			writeMask = WriteMask.COLOR_DEPTH;
			useOverlay = true;
			useLight = true;
			cardinalLightingMode = CardinalLightingMode.ENTITY;
		}

		public Builder(Material material) {
			copyFrom(material);
		}

		public Builder copyFrom(Material material) {
			shaders = material.shaders();
			fog = material.fog();
			cutout = material.cutout();
			light = material.light();
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
			cardinalLightingMode = material.cardinalLightingMode();
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

		public Builder light(LightShader value) {
			this.light = value;
			return this;
		}

		public Builder texture(Identifier value) {
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
			return cardinalLightingMode(value ? CardinalLightingMode.ENTITY : CardinalLightingMode.OFF);
		}

		public Builder cardinalLightingMode(CardinalLightingMode value) {
			this.cardinalLightingMode = value;
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
		public LightShader light() {
			return light;
		}

		@Override
		public Identifier texture() {
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
		public CardinalLightingMode cardinalLightingMode() {
			return cardinalLightingMode;
		}

		public SimpleMaterial build() {
			return new SimpleMaterial(this);
		}
	}
}
