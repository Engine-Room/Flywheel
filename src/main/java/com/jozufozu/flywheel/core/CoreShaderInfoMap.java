package com.jozufozu.flywheel.core;

import static com.jozufozu.flywheel.core.CoreShaderInfoMap.CoreShaderInfo.FogType.COLOR_FOG;
import static com.jozufozu.flywheel.core.CoreShaderInfoMap.CoreShaderInfo.FogType.FADE_FOG;
import static com.jozufozu.flywheel.core.CoreShaderInfoMap.CoreShaderInfo.FogType.NO_FOG;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.backend.ShadersModHandler;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.ShaderInstance;

public class CoreShaderInfoMap {
	private static final Map<String, CoreShaderInfo> MAP = new HashMap<>();

	static {
		registerInfo("block", new CoreShaderInfo(-1, false, NO_FOG));
		registerInfo("new_entity", new CoreShaderInfo(-1, false, NO_FOG));
		registerInfo("particle", new CoreShaderInfo(0.1f, false, COLOR_FOG));
		registerInfo("position", new CoreShaderInfo(-1, false, COLOR_FOG));
		registerInfo("position_color", new CoreShaderInfo(0, false, NO_FOG));
		registerInfo("position_color_lightmap", new CoreShaderInfo(-1, false, NO_FOG));
		registerInfo("position_color_tex", new CoreShaderInfo(0.1f, false, NO_FOG));
		registerInfo("position_color_tex_lightmap", new CoreShaderInfo(0.1f, false, NO_FOG));
		registerInfo("position_tex", new CoreShaderInfo(0, false, NO_FOG));
		registerInfo("position_tex_color", new CoreShaderInfo(0.1f, false, NO_FOG));
		registerInfo("position_tex_color_normal", new CoreShaderInfo(0.1f, false, COLOR_FOG));
		registerInfo("position_tex_lightmap_color", new CoreShaderInfo(0.1f, false, NO_FOG));
		registerInfo("rendertype_solid", new CoreShaderInfo(-1, false, COLOR_FOG));
		registerInfo("rendertype_cutout_mipped", new CoreShaderInfo(ShadersModHandler.isShaderPackInUse() ? 0.1f : 0.5f, false, COLOR_FOG));
		registerInfo("rendertype_cutout", new CoreShaderInfo(0.1f, false, COLOR_FOG));
		registerInfo("rendertype_translucent", new CoreShaderInfo(-1, false, COLOR_FOG));
		registerInfo("rendertype_translucent_moving_block", new CoreShaderInfo(-1, false, NO_FOG));
		registerInfo("rendertype_translucent_no_crumbling", new CoreShaderInfo(-1, false, NO_FOG));
		registerInfo("rendertype_armor_cutout_no_cull", new CoreShaderInfo(0.1f, true, COLOR_FOG));
		registerInfo("rendertype_entity_solid", new CoreShaderInfo(-1, true, COLOR_FOG));
		registerInfo("rendertype_entity_cutout", new CoreShaderInfo(0.1f, true, COLOR_FOG));
		registerInfo("rendertype_entity_cutout_no_cull", new CoreShaderInfo(0.1f, true, COLOR_FOG));
		registerInfo("rendertype_entity_cutout_no_cull_z_offset", new CoreShaderInfo(0.1f, true, COLOR_FOG));
		registerInfo("rendertype_item_entity_translucent_cull", new CoreShaderInfo(0.1f, true, COLOR_FOG));
		registerInfo("rendertype_entity_translucent_cull", new CoreShaderInfo(0.1f, true, COLOR_FOG));
		registerInfo("rendertype_entity_translucent", new CoreShaderInfo(0.1f, true, COLOR_FOG));
		registerInfo("rendertype_entity_smooth_cutout", new CoreShaderInfo(0.1f, true, COLOR_FOG));
		registerInfo("rendertype_beacon_beam", new CoreShaderInfo(-1, false, COLOR_FOG));
		registerInfo("rendertype_entity_decal", new CoreShaderInfo(0.1f, true, COLOR_FOG));
		registerInfo("rendertype_entity_no_outline", new CoreShaderInfo(-1, true, COLOR_FOG));
		registerInfo("rendertype_entity_shadow", new CoreShaderInfo(-1, false, COLOR_FOG));
		// Special alpha discard
		registerInfo("rendertype_entity_alpha", new CoreShaderInfo(-1, false, NO_FOG));
		registerInfo("rendertype_eyes", new CoreShaderInfo(-1, false, FADE_FOG));
		registerInfo("rendertype_energy_swirl", new CoreShaderInfo(0.1f, false, FADE_FOG));
		registerInfo("rendertype_leash", new CoreShaderInfo(-1, false, COLOR_FOG));
		registerInfo("rendertype_water_mask", new CoreShaderInfo(-1, false, NO_FOG));
		registerInfo("rendertype_outline", new CoreShaderInfo(0, false, NO_FOG));
		registerInfo("rendertype_armor_glint", new CoreShaderInfo(0.1f, false, FADE_FOG));
		registerInfo("rendertype_armor_entity_glint", new CoreShaderInfo(0.1f, false, FADE_FOG));
		registerInfo("rendertype_glint_translucent", new CoreShaderInfo(0.1f, false, FADE_FOG));
		registerInfo("rendertype_glint", new CoreShaderInfo(0.1f, false, FADE_FOG));
		registerInfo("rendertype_glint_direct", new CoreShaderInfo(0.1f, false, FADE_FOG));
		registerInfo("rendertype_entity_glint", new CoreShaderInfo(0.1f, false, FADE_FOG));
		registerInfo("rendertype_entity_glint_direct", new CoreShaderInfo(0.1f, false, FADE_FOG));
		registerInfo("rendertype_text", new CoreShaderInfo(0.1f, false, COLOR_FOG));
		registerInfo("rendertype_text_intensity", new CoreShaderInfo(0.1f, false, COLOR_FOG));
		registerInfo("rendertype_text_see_through", new CoreShaderInfo(0.1f, false, NO_FOG));
		registerInfo("rendertype_text_intensity_see_through", new CoreShaderInfo(0.1f, false, NO_FOG));
		registerInfo("rendertype_lightning", new CoreShaderInfo(-1, false, FADE_FOG));
		registerInfo("rendertype_tripwire", new CoreShaderInfo(0.1f, false, COLOR_FOG));
		registerInfo("rendertype_end_portal", new CoreShaderInfo(-1, false, NO_FOG));
		registerInfo("rendertype_end_gateway", new CoreShaderInfo(-1, false, NO_FOG));
		registerInfo("rendertype_lines", new CoreShaderInfo(-1, false, COLOR_FOG));
		registerInfo("rendertype_crumbling", new CoreShaderInfo(0.1f, false, NO_FOG));

		registerInfo("forge:rendertype_entity_unlit_translucent", new CoreShaderInfo(0.1f, false, COLOR_FOG));
	}

	public static void registerInfo(String name, CoreShaderInfo info) {
		MAP.put(name, info);
	}

	@Nullable
	public static CoreShaderInfo getInfo(String name) {
		return MAP.get(name);
	}

	public record CoreShaderInfo(float alphaDiscard, boolean appliesDiffuse, FogType fogType) {
		public static final CoreShaderInfo DEFAULT = new CoreShaderInfo(-1, false, NO_FOG);

		public static CoreShaderInfo get() {
			CoreShaderInfo out = null;
			ShaderInstance coreShader = RenderSystem.getShader();
			if (coreShader != null) {
				String coreShaderName = coreShader.getName();
				out = getInfo(coreShaderName);
			}
			if (out == null) {
				out = DEFAULT;
			}
			return out;
		}

		public float getAdjustedAlphaDiscard() {
			float alphaDiscard = alphaDiscard();
			if (alphaDiscard == 0) {
				alphaDiscard = 0.0001f;
			} else if (alphaDiscard < 0) {
				alphaDiscard = 0;
			}
			return alphaDiscard;
		}

		public enum FogType {
			NO_FOG,
			COLOR_FOG,
			FADE_FOG;
		}
	}
}
