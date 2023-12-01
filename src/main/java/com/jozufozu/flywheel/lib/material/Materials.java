package com.jozufozu.flywheel.lib.material;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.material.MaterialVertexTransformer;
import com.jozufozu.flywheel.api.material.Transparency;
import com.jozufozu.flywheel.lib.math.DiffuseLightCalculator;
import com.jozufozu.flywheel.lib.util.ShadersModHandler;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
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
			.baseTexture(InventoryMenu.BLOCK_ATLAS)
			.mip(true)
			.shaders(StandardMaterialShaders.SHADED)
			.fallbackRenderType(RenderType.solid())
			.vertexTransformer(SHADING_TRANSFORMER)
			.build();
	public static final Material CHUNK_SOLID_UNSHADED = SimpleMaterial.builder()
			.diffuse(false)
			.baseTexture(InventoryMenu.BLOCK_ATLAS)
			.mip(true)
			.fallbackRenderType(RenderType.solid())
			.build();

	public static final Material CHUNK_CUTOUT_MIPPED_SHADED = SimpleMaterial.builder()
			.baseTexture(InventoryMenu.BLOCK_ATLAS)
			.mip(true)
			.shaders(StandardMaterialShaders.SHADED_CUTOUT)
			.fallbackRenderType(RenderType.cutoutMipped())
			.vertexTransformer(SHADING_TRANSFORMER)
			.build();
	public static final Material CHUNK_CUTOUT_MIPPED_UNSHADED = SimpleMaterial.builder()
			.diffuse(false)
			.baseTexture(InventoryMenu.BLOCK_ATLAS)
			.mip(true)
			.shaders(StandardMaterialShaders.CUTOUT)
			.fallbackRenderType(RenderType.cutoutMipped())
			.build();

	public static final Material CHUNK_CUTOUT_SHADED = SimpleMaterial.builder()
			.baseTexture(InventoryMenu.BLOCK_ATLAS)
			.mip(false)
			.shaders(StandardMaterialShaders.SHADED_CUTOUT)
			.fallbackRenderType(RenderType.cutout())
			.vertexTransformer(SHADING_TRANSFORMER)
			.build();
	public static final Material CHUNK_CUTOUT_UNSHADED = SimpleMaterial.builder()
			.diffuse(false)
			.baseTexture(InventoryMenu.BLOCK_ATLAS)
			.mip(false)
			.shaders(StandardMaterialShaders.CUTOUT)
			.fallbackRenderType(RenderType.cutout())
			.build();

	public static final Material CHUNK_TRANSLUCENT_SHADED = SimpleMaterial.builder()
			.baseTexture(InventoryMenu.BLOCK_ATLAS)
			.mip(true)
			.transparency(Transparency.TRANSLUCENT)
			.shaders(StandardMaterialShaders.SHADED)
			.fallbackRenderType(RenderType.translucent())
			.vertexTransformer(SHADING_TRANSFORMER)
			.build();
	public static final Material CHUNK_TRANSLUCENT_UNSHADED = SimpleMaterial.builder()
			.diffuse(false)
			.baseTexture(InventoryMenu.BLOCK_ATLAS)
			.mip(true)
			.transparency(Transparency.TRANSLUCENT)
			.fallbackRenderType(RenderType.translucent())
			.build();

	public static final Material CHUNK_TRIPWIRE_SHADED = SimpleMaterial.builder()
			.baseTexture(InventoryMenu.BLOCK_ATLAS)
			.mip(true)
			.transparency(Transparency.TRANSLUCENT)
			.shaders(StandardMaterialShaders.SHADED_CUTOUT)
			.fallbackRenderType(RenderType.tripwire())
			.vertexTransformer(SHADING_TRANSFORMER)
			.build();
	public static final Material CHUNK_TRIPWIRE_UNSHADED = SimpleMaterial.builder()
			.diffuse(false)
			.baseTexture(InventoryMenu.BLOCK_ATLAS)
			.mip(true)
			.transparency(Transparency.TRANSLUCENT)
			.shaders(StandardMaterialShaders.CUTOUT)
			.fallbackRenderType(RenderType.tripwire())
			.build();

	public static final Material CHEST = SimpleMaterial.builder()
			.baseTexture(Sheets.CHEST_SHEET)
			.mip(false)
			.shaders(StandardMaterialShaders.SHADED)
			.fallbackRenderType(Sheets.chestSheet())
			.build();
	public static final Material SHULKER = SimpleMaterial.builder()
			.baseTexture(Sheets.SHULKER_SHEET)
			.mip(false)
			.backfaceCull(false)
			.shaders(StandardMaterialShaders.SHADED_CUTOUT)
			.fallbackRenderType(Sheets.shulkerBoxSheet())
			.build();
	public static final Material BELL = SimpleMaterial.builder()
			.baseTexture(InventoryMenu.BLOCK_ATLAS)
			.mip(false)
			.shaders(StandardMaterialShaders.SHADED)
			.fallbackRenderType(Sheets.solidBlockSheet())
			.build();
	public static final Material MINECART = SimpleMaterial.builder()
			.baseTexture(MINECART_LOCATION)
			.mip(false)
			.shaders(StandardMaterialShaders.SHADED)
			.fallbackRenderType(RenderType.entitySolid(MINECART_LOCATION))
			.build();

	private Materials() {
	}
}
