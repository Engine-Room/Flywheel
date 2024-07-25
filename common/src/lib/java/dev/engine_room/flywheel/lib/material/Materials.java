package dev.engine_room.flywheel.lib.material;

import dev.engine_room.flywheel.api.material.DepthTest;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.api.material.WriteMask;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
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

	public static final Material GLINT = SimpleMaterial.builder()
			.texture(ItemRenderer.ENCHANTED_GLINT_ITEM)
			.shaders(StandardMaterialShaders.GLINT)
			.transparency(Transparency.GLINT)
			.writeMask(WriteMask.COLOR)
			.depthTest(DepthTest.EQUAL)
			.backfaceCulling(false)
			.blur(true)
			.mipmap(false)
			.build();

	public static final Material GLINT_ENTITY = SimpleMaterial.builderOf(GLINT)
			.texture(ItemRenderer.ENCHANTED_GLINT_ENTITY)
			.build();

	private Materials() {
	}
}
