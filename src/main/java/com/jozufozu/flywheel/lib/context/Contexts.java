package com.jozufozu.flywheel.lib.context;

import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.context.ContextShader;
import com.jozufozu.flywheel.api.context.Shader;
import com.jozufozu.flywheel.api.context.Textures;
import com.jozufozu.flywheel.api.material.Material;

import net.minecraft.client.resources.model.ModelBakery;

public final class Contexts {
	public static final Context DEFAULT = new Context() {
		@Override
		public ContextShader contextShader() {
			return ContextShaders.DEFAULT;
		}

		@Override
		public void prepare(Material material, Shader shader, Textures textures) {
			var texture = textures.byName(material.texture());
			texture.filter(material.blur(), material.mipmap());
			shader.setTexture("_flw_diffuseTex", texture);

			shader.setTexture("_flw_overlayTex", textures.overlay());
			shader.setTexture("_flw_lightTex", textures.light());
		}
	};

	public static final Context CRUMBLING = new Context() {
		@Override
		public ContextShader contextShader() {
			return ContextShaders.CRUMBLING;
		}

		@Override
		public void prepare(Material material, Shader shader, Textures textures) {
			var texture = textures.byName(material.texture());
			texture.filter(material.blur(), material.mipmap());
			shader.setTexture("_flw_diffuseTex", texture);

			var crumblingTexture = textures.byName(ModelBakery.BREAKING_LOCATIONS.get(0));
			shader.setTexture("_flw_crumblingTex", crumblingTexture);
		}
	};

	private Contexts() {
	}
}
