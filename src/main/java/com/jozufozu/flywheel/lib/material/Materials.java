package com.jozufozu.flywheel.lib.material;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.material.MaterialVertexTransformer;
import com.jozufozu.flywheel.api.material.Transparency;
import com.jozufozu.flywheel.lib.math.DiffuseLightCalculator;
import com.jozufozu.flywheel.lib.util.ShadersModHandler;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;

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
			.useOverlay(false)
			.fallbackRenderType(RenderType.solid())
			.vertexTransformer(SHADING_TRANSFORMER)
			.build();
	public static final Material CHUNK_SOLID_UNSHADED = SimpleMaterial.builder()
			.useOverlay(false)
			.diffuse(false)
			.fallbackRenderType(RenderType.solid())
			.build();

	public static final Material CHUNK_CUTOUT_MIPPED_SHADED = SimpleMaterial.builder()
			.cutout(CutoutShaders.HALF)
			.useOverlay(false)
			.fallbackRenderType(RenderType.cutoutMipped())
			.vertexTransformer(SHADING_TRANSFORMER)
			.build();
	public static final Material CHUNK_CUTOUT_MIPPED_UNSHADED = SimpleMaterial.builder()
			.cutout(CutoutShaders.HALF)
			.useOverlay(false)
			.diffuse(false)
			.fallbackRenderType(RenderType.cutoutMipped())
			.build();

	public static final Material CHUNK_CUTOUT_SHADED = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.mipmap(false)
			.useOverlay(false)
			.fallbackRenderType(RenderType.cutout())
			.vertexTransformer(SHADING_TRANSFORMER)
			.build();
	public static final Material CHUNK_CUTOUT_UNSHADED = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.mipmap(false)
			.useOverlay(false)
			.diffuse(false)
			.fallbackRenderType(RenderType.cutout())
			.build();

	public static final Material CHUNK_TRANSLUCENT_SHADED = SimpleMaterial.builder()
			.transparency(Transparency.TRANSLUCENT)
			.useOverlay(false)
			.fallbackRenderType(RenderType.translucent())
			.vertexTransformer(SHADING_TRANSFORMER)
			.build();
	public static final Material CHUNK_TRANSLUCENT_UNSHADED = SimpleMaterial.builder()
			.transparency(Transparency.TRANSLUCENT)
			.useOverlay(false)
			.diffuse(false)
			.fallbackRenderType(RenderType.translucent())
			.build();

	public static final Material CHUNK_TRIPWIRE_SHADED = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.transparency(Transparency.TRANSLUCENT)
			.useOverlay(false)
			.fallbackRenderType(RenderType.tripwire())
			.vertexTransformer(SHADING_TRANSFORMER)
			.build();
	public static final Material CHUNK_TRIPWIRE_UNSHADED = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.transparency(Transparency.TRANSLUCENT)
			.useOverlay(false)
			.diffuse(false)
			.fallbackRenderType(RenderType.tripwire())
			.build();

	public static final Material CHEST = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.texture(Sheets.CHEST_SHEET)
			.mipmap(false)
			.fallbackRenderType(Sheets.chestSheet())
			.build();
	public static final Material SHULKER = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.texture(Sheets.SHULKER_SHEET)
			.mipmap(false)
			.backfaceCulling(false)
			.fallbackRenderType(Sheets.shulkerBoxSheet())
			.build();
	public static final Material BELL = SimpleMaterial.builder()
			.mipmap(false)
			.fallbackRenderType(Sheets.solidBlockSheet())
			.build();
	public static final Material MINECART = SimpleMaterial.builder()
			.texture(MINECART_LOCATION)
			.mipmap(false)
			.fallbackRenderType(RenderType.entitySolid(MINECART_LOCATION))
			.build();

	private Materials() {
	}
}
