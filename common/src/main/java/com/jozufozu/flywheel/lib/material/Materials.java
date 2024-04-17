package com.jozufozu.flywheel.lib.material;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.material.Transparency;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;

public final class Materials {
	private static final ResourceLocation MINECART_LOCATION = new ResourceLocation("textures/entity/minecart.png");

	public static final Material CHUNK_SOLID_SHADED = SimpleMaterial.builder()
			.build();
	public static final Material CHUNK_SOLID_UNSHADED = SimpleMaterial.builder()
			.diffuse(false)
			.build();

	public static final Material CHUNK_CUTOUT_MIPPED_SHADED = SimpleMaterial.builder()
			.cutout(CutoutShaders.HALF)
			.build();
	public static final Material CHUNK_CUTOUT_MIPPED_UNSHADED = SimpleMaterial.builder()
			.cutout(CutoutShaders.HALF)
			.diffuse(false)
			.build();

	public static final Material CHUNK_CUTOUT_SHADED = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.mipmap(false)
			.build();
	public static final Material CHUNK_CUTOUT_UNSHADED = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.mipmap(false)
			.diffuse(false)
			.build();

	public static final Material CHUNK_TRANSLUCENT_SHADED = SimpleMaterial.builder()
			.transparency(Transparency.TRANSLUCENT)
			.build();
	public static final Material CHUNK_TRANSLUCENT_UNSHADED = SimpleMaterial.builder()
			.transparency(Transparency.TRANSLUCENT)
			.diffuse(false)
			.build();

	public static final Material CHUNK_TRIPWIRE_SHADED = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.transparency(Transparency.TRANSLUCENT)
			.build();
	public static final Material CHUNK_TRIPWIRE_UNSHADED = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.transparency(Transparency.TRANSLUCENT)
			.diffuse(false)
			.build();

	public static final Material CHEST = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.texture(Sheets.CHEST_SHEET)
			.mipmap(false)
			.build();
	public static final Material SHULKER = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.texture(Sheets.SHULKER_SHEET)
			.mipmap(false)
			.backfaceCulling(false)
			.build();
	public static final Material BELL = SimpleMaterial.builder()
			.mipmap(false)
			.build();
	public static final Material MINECART = SimpleMaterial.builder()
			.texture(MINECART_LOCATION)
			.mipmap(false)
			.build();

	private Materials() {
	}
}
