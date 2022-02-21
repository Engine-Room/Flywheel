package com.jozufozu.flywheel.fabric.model;

import java.lang.reflect.Field;

import com.jozufozu.flywheel.Flywheel;

import io.vram.frex.api.material.MaterialConstants;
import io.vram.frex.fabric.compat.FabricMaterial;
import io.vram.frex.fabric.compat.FabricQuadView;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.impl.client.indigo.renderer.RenderMaterialImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.QuadViewImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockState;

public class FabricModelUtil {
	public static final boolean INDIUM_LOADED = FabricLoader.getInstance().isModLoaded("indium");
	public static final boolean FREX_LOADED = FabricLoader.getInstance().isModLoaded("frex");

	private static final BlendModeGetter BLEND_MODE_GETTER = createBlendModeGetter();
	private static final ShadedPredicate SHADED_PREDICATE = createShadedPredicate();

	private static BlendModeGetter createBlendModeGetter() {
		if (FREX_LOADED) {
			try {
				Field frexMaterialField = FabricMaterial.class.getDeclaredField("wrapped");
				frexMaterialField.setAccessible(true);
				return material -> {
					try {
						io.vram.frex.api.material.RenderMaterial frexMaterial = (io.vram.frex.api.material.RenderMaterial) frexMaterialField.get(material);
						return switch (frexMaterial.preset()) {
						case MaterialConstants.PRESET_DEFAULT -> BlendMode.DEFAULT;
						case MaterialConstants.PRESET_SOLID -> BlendMode.SOLID;
						case MaterialConstants.PRESET_CUTOUT_MIPPED -> BlendMode.CUTOUT_MIPPED;
						case MaterialConstants.PRESET_CUTOUT -> BlendMode.CUTOUT;
						case MaterialConstants.PRESET_TRANSLUCENT -> BlendMode.TRANSLUCENT;
						case MaterialConstants.PRESET_NONE -> {
							if (frexMaterial.transparency() != MaterialConstants.TRANSPARENCY_NONE) {
								yield BlendMode.TRANSLUCENT;
							} else if (frexMaterial.cutout() == MaterialConstants.CUTOUT_NONE) {
								yield BlendMode.SOLID;
							} else {
								yield frexMaterial.unmipped() ? BlendMode.CUTOUT : BlendMode.CUTOUT_MIPPED;
							}
						}
						default -> BlendMode.DEFAULT;
						};
					} catch (Exception e) {
					}
					return BlendMode.DEFAULT;
				};
			} catch (Exception e) {
				Flywheel.LOGGER.error("Detected FREX but failed to load material wrapper field.", e);
				return material -> BlendMode.DEFAULT;
			}
		} else if (INDIUM_LOADED) {
			return material -> ((link.infra.indium.renderer.RenderMaterialImpl) material).blendMode(0);
		} else {
			return material -> ((RenderMaterialImpl) material).blendMode(0);
		}
	}

	private static ShadedPredicate createShadedPredicate() {
		if (FREX_LOADED) {
			try {
				Field frexQuadViewField = FabricQuadView.class.getDeclaredField("wrapped");
				frexQuadViewField.setAccessible(true);
				return quad -> {
					try {
						io.vram.frex.api.mesh.QuadView frexQuadView = (io.vram.frex.api.mesh.QuadView) frexQuadViewField.get(quad);
						return !frexQuadView.material().disableDiffuse();
					} catch (Exception e) {
					}
					return true;
				};
			} catch (Exception e) {
				Flywheel.LOGGER.error("Detected FREX but failed to load quad view wrapper field.", e);
				return quad -> true;
			}
		} else if (INDIUM_LOADED) {
			return quad -> ((link.infra.indium.renderer.mesh.QuadViewImpl) quad).hasShade();
		} else {
			return quad -> ((QuadViewImpl) quad).hasShade();
		}
	}

	public static BlendMode getBlendMode(RenderMaterial material) {
		return BLEND_MODE_GETTER.getBlendMode(material);
	}

	public static boolean isShaded(QuadView quad) {
		return SHADED_PREDICATE.isShaded(quad);
	}

	public static boolean doesLayerMatch(BlockState modelState, RenderType layer) {
		return ItemBlockRenderTypes.getChunkRenderType(modelState) == layer;
	}

	private interface BlendModeGetter {
		BlendMode getBlendMode(RenderMaterial material);
	}

	private interface ShadedPredicate {
		boolean isShaded(QuadView quad);
	}
}
