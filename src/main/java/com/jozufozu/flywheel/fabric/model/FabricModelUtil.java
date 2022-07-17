package com.jozufozu.flywheel.fabric.model;

import java.lang.reflect.Field;

import com.jozufozu.flywheel.Flywheel;

import io.vram.frex.api.material.MaterialConstants;
import io.vram.frex.fabric.compat.FabricQuadView;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.impl.client.indigo.renderer.IndigoRenderer;
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
		if (FabricLoader.getInstance().isModLoaded("frex")) {
			try {
				Field frexQuadField = FabricQuadView.class.getDeclaredField("wrapped");
				frexQuadField.setAccessible(true);
				return quad -> {
					try {
						io.vram.frex.api.material.RenderMaterial frexMaterial = ((io.vram.frex.api.mesh.QuadView) frexQuadField.get(quad)).material();
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
						//
					}
					return BlendMode.DEFAULT;
				};
			} catch (Exception e) {
				Flywheel.LOGGER.error("Detected FREX but failed to load quad wrapper field", e);
			}
		} else if (FabricLoader.getInstance().isModLoaded("indium")) {
			return quad -> ((link.infra.indium.renderer.RenderMaterialImpl) quad.material()).blendMode(0);
		} else if (RendererAccess.INSTANCE.getRenderer() instanceof IndigoRenderer) {
			return quad -> ((RenderMaterialImpl) quad.material()).blendMode(0);
		}
		return quad -> BlendMode.DEFAULT;
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
						//
					}
					return true;
				};
			} catch (Exception e) {
				Flywheel.LOGGER.error("Detected FREX but failed to load quad view wrapper field.", e);
			}
		} else if (INDIUM_LOADED) {
			return quad -> ((link.infra.indium.renderer.mesh.QuadViewImpl) quad).hasShade();
		} else if (RendererAccess.INSTANCE.getRenderer() instanceof IndigoRenderer) {
			return quad -> ((QuadViewImpl) quad).hasShade();
		}
		return quad -> true;
	}

	public static BlendMode getBlendMode(QuadView quad) {
		return BLEND_MODE_GETTER.getBlendMode(quad);
	}

	public static boolean isShaded(QuadView quad) {
		return SHADED_PREDICATE.isShaded(quad);
	}

	public static boolean doesLayerMatch(BlockState modelState, RenderType layer) {
		return ItemBlockRenderTypes.getChunkRenderType(modelState) == layer;
	}

	private interface BlendModeGetter {
		BlendMode getBlendMode(QuadView quad);
	}

	private interface ShadedPredicate {
		boolean isShaded(QuadView quad);
	}
}
