package com.jozufozu.flywheel.lib.material;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.material.MaterialVertexTransformer;
import com.jozufozu.flywheel.gl.GlTextureUnit;
import com.jozufozu.flywheel.lib.material.SimpleMaterial.GlStateShard;
import com.jozufozu.flywheel.lib.math.DiffuseLightCalculator;
import com.jozufozu.flywheel.lib.util.ShadersModHandler;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public final class Materials {
	public static final MaterialVertexTransformer SHADING_TRANSFORMER = (vertexList, level) -> {
		if (ShadersModHandler.isShaderPackInUse()) {
			return;
		}

		DiffuseLightCalculator diffuseCalc = DiffuseLightCalculator.forLevel(level);
		for (int i = 0; i < vertexList.vertexCount(); i++) {
			float diffuse = diffuseCalc.getDiffuse(vertexList.normalX(i), vertexList.normalY(i), vertexList.normalZ(i), true);
			vertexList.r(i, vertexList.r(i) * diffuse);
			vertexList.g(i, vertexList.g(i) * diffuse);
			vertexList.b(i, vertexList.b(i) * diffuse);
		}
	};

	private static final ResourceLocation MINECART_LOCATION = new ResourceLocation("textures/entity/minecart.png");

	public static final Material CHUNK_SOLID_SHADED = SimpleMaterial.builder()
			.addShard(Shards.diffuseTex(InventoryMenu.BLOCK_ATLAS, false, true))
			.shaders(StandardMaterialShaders.SHADED)
			.fallbackRenderType(RenderType.solid())
			.vertexTransformer(SHADING_TRANSFORMER)
			.build();
	public static final Material CHUNK_SOLID_UNSHADED = SimpleMaterial.builder()
			.addShard(Shards.diffuseTex(InventoryMenu.BLOCK_ATLAS, false, true))
			.fallbackRenderType(RenderType.solid())
			.build();

	public static final Material CHUNK_CUTOUT_MIPPED_SHADED = SimpleMaterial.builder()
			.addShard(Shards.diffuseTex(InventoryMenu.BLOCK_ATLAS, false, true))
			.shaders(StandardMaterialShaders.SHADED_CUTOUT)
			.fallbackRenderType(RenderType.cutoutMipped())
			.vertexTransformer(SHADING_TRANSFORMER)
			.build();
	public static final Material CHUNK_CUTOUT_MIPPED_UNSHADED = SimpleMaterial.builder()
			.addShard(Shards.diffuseTex(InventoryMenu.BLOCK_ATLAS, false, true))
			.shaders(StandardMaterialShaders.CUTOUT)
			.fallbackRenderType(RenderType.cutoutMipped())
			.build();

	public static final Material CHUNK_CUTOUT_SHADED = SimpleMaterial.builder()
			.addShard(Shards.diffuseTex(InventoryMenu.BLOCK_ATLAS, false, false))
			.shaders(StandardMaterialShaders.SHADED_CUTOUT)
			.fallbackRenderType(RenderType.cutout())
			.vertexTransformer(SHADING_TRANSFORMER)
			.build();
	public static final Material CHUNK_CUTOUT_UNSHADED = SimpleMaterial.builder()
			.addShard(Shards.diffuseTex(InventoryMenu.BLOCK_ATLAS, false, false))
			.shaders(StandardMaterialShaders.CUTOUT)
			.fallbackRenderType(RenderType.cutout())
			.build();

	public static final Material CHUNK_TRANSLUCENT_SHADED = SimpleMaterial.builder()
			.addShard(Shards.diffuseTex(InventoryMenu.BLOCK_ATLAS, false, true))
			.addShard(Shards.TRANSLUCENT_TRANSPARENCY)
			.shaders(StandardMaterialShaders.SHADED)
			.fallbackRenderType(RenderType.translucent())
			.vertexTransformer(SHADING_TRANSFORMER)
			.build();
	public static final Material CHUNK_TRANSLUCENT_UNSHADED = SimpleMaterial.builder()
			.addShard(Shards.diffuseTex(InventoryMenu.BLOCK_ATLAS, false, true))
			.addShard(Shards.TRANSLUCENT_TRANSPARENCY)
			.fallbackRenderType(RenderType.translucent())
			.build();

	public static final Material CHUNK_TRIPWIRE_SHADED = SimpleMaterial.builder()
			.addShard(Shards.diffuseTex(InventoryMenu.BLOCK_ATLAS, false, true))
			.addShard(Shards.TRANSLUCENT_TRANSPARENCY)
			.shaders(StandardMaterialShaders.SHADED_CUTOUT)
			.fallbackRenderType(RenderType.tripwire())
			.vertexTransformer(SHADING_TRANSFORMER)
			.build();
	public static final Material CHUNK_TRIPWIRE_UNSHADED = SimpleMaterial.builder()
			.addShard(Shards.diffuseTex(InventoryMenu.BLOCK_ATLAS, false, true))
			.addShard(Shards.TRANSLUCENT_TRANSPARENCY)
			.shaders(StandardMaterialShaders.CUTOUT)
			.fallbackRenderType(RenderType.tripwire())
			.build();

	public static final Material CHEST = SimpleMaterial.builder()
			.addShard(Shards.diffuseTex(Sheets.CHEST_SHEET, false, false))
			.shaders(StandardMaterialShaders.SHADED)
			.fallbackRenderType(Sheets.chestSheet())
			.build();
	public static final Material SHULKER = SimpleMaterial.builder()
			.addShard(Shards.diffuseTex(Sheets.SHULKER_SHEET, false, false))
			.addShard(Shards.DISABLE_CULL)
			.shaders(StandardMaterialShaders.SHADED_CUTOUT)
			.fallbackRenderType(Sheets.shulkerBoxSheet())
			.build();
	public static final Material BELL = SimpleMaterial.builder()
			.addShard(Shards.diffuseTex(InventoryMenu.BLOCK_ATLAS, false, false))
			.shaders(StandardMaterialShaders.SHADED)
			.fallbackRenderType(Sheets.solidBlockSheet())
			.build();
	public static final Material MINECART = SimpleMaterial.builder()
			.addShard(Shards.diffuseTex(MINECART_LOCATION, false, false))
			.shaders(StandardMaterialShaders.SHADED)
			.fallbackRenderType(RenderType.entitySolid(MINECART_LOCATION))
			.build();

	private Materials() {
	}

	public static final class Shards {
		public static final GlStateShard DISABLE_CULL = new GlStateShard(
				() -> {
					RenderSystem.disableCull();
				},
				() -> {
					RenderSystem.enableCull();
				}
		);

		public static final GlStateShard TRANSLUCENT_TRANSPARENCY = new GlStateShard(
				() -> {
					RenderSystem.enableBlend();
					RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				},
				() -> {
					RenderSystem.disableBlend();
					RenderSystem.defaultBlendFunc();
				}
		);

		public static GlStateShard diffuseTex(ResourceLocation loc, boolean blur, boolean mipmap) {
			return new GlStateShard(
					() -> {
						GlTextureUnit.T0.makeActive();
						AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(loc);
						texture.setFilter(blur, mipmap);
						RenderSystem.setShaderTexture(0, texture.getId());
					},
					() -> {
						GlTextureUnit.T0.makeActive();
						RenderSystem.setShaderTexture(0, 0);
					}
			);
		}
	}
}
